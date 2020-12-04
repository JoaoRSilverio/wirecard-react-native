
package com.rnwirecard;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
// test only imports
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
// -------------------------
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.wirecard.ecom.model.Status;
import com.wirecard.ecom.Client;
import com.wirecard.ecom.model.Notification;
import com.wirecard.ecom.model.Notifications;
import com.wirecard.ecom.model.CardToken;
import com.wirecard.ecom.model.Payment;
import com.wirecard.ecom.model.TransactionType;
import com.wirecard.ecom.model.TransactionState;
import com.wirecard.ecom.model.out.PaymentResponse;
import com.wirecard.ecom.ResponseCode;
import com.wirecard.ecom.card.model.CardPayment;
import com.wirecard.ecom.googlepay.model.GooglePayPayment;

public class RNWirecardModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    public final static int REQUEST_TIMEOUT = 30;
    public Callback onPaymentFailure;
    public Callback onPaymentSuccess;
    public Client wirecardClient;
    private final ReactApplicationContext reactContext;

    public RNWirecardModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "RNWirecard";
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @ReactMethod
    public void initiateClient(String environment, Callback onSuccess, Callback onFailure) {
        try {
            wirecardClient = new Client(this.getCurrentActivity(), environment, REQUEST_TIMEOUT);
            onSuccess.invoke();
        } catch (Exception e) {
            Log.i("wirecard-react-native", "client failed to initiate with endpoint: " + environment);
            Log.i("wirecard-react-native", e.getMessage());
            onFailure.invoke();
        }
    }

    @ReactMethod
    public void testBridge(String received) {
        Log.i("wirecard-react-native", received);
    }

    @ReactMethod
    public void newPaymentRequest(ReadableMap payment, Callback onPaymentAccepted, Callback onPaymentRejected) {
        this.onPaymentFailure = onPaymentRejected;
        this.onPaymentSuccess = onPaymentAccepted;
        this.startRequestedPayment(payment);
    }

    public void startRequestedPayment(ReadableMap paymentInfo) {
        Log.i("wirecard-react-native", "initiating actual transaction");
        switch (paymentInfo.getString("paymentMethod")) {
        case "card":
            CardPayment cardPayment = this.createCardPayment(paymentInfo);
            wirecardClient.startPayment(cardPayment);
            break;
        case "paypal":
            // PayPalPayment payPalPayment = this.createPayPalPayment(paymentInfo);
            // wirecardClient.startPayment(payPalPayment);
            break;
        case "sepa":
            // SepaPayment sepaPayment = this.createSepaPayment(paymentInfo);
            // wirecardClient.startPayment(sepaPayment);
            break;
        case "zapp":
            // ZappPayment zappPayment = this.createZappPayment(paymentInfo);
            // wirecardClient.startPayment(zappPayment);
            break;
        case "gpay":
            GooglePayPayment gPayment = this.createGPayPayment(paymentInfo);
            wirecardClient.startPayment(gPayment);
            break;

        default:
            CardPayment defaultCardPayment = this.createCardPayment(paymentInfo);
            break;
        }
    }

    // .setTransactionType(paymentInfo.getString("transactionType"))
    public CardPayment createCardPayment(ReadableMap paymentInfo) {

        CardPayment wirecardPayment = new CardPayment.Builder().setSignature(paymentInfo.getString("signature"))
                .setMerchantAccountId(paymentInfo.getString("merchantID"))
                .setTransactionType(this.getTransactionType(paymentInfo.getString("transactionType")))
                .setRequestId(paymentInfo.getString("requestID")).setAmount(getAmount(paymentInfo.getString("amount")))
                .setCurrency(paymentInfo.getString("currency")).build();
        if (paymentInfo.hasKey("token")) {
            String token = paymentInfo.getString("token");
            String maskedAccountNumber = paymentInfo.getString("maskedAccountNumber");
            CardToken cardToken = new CardToken();
            cardToken.setMaskedAccountNumber(maskedAccountNumber);
            cardToken.setTokenId(token);
            wirecardPayment.setCardToken(cardToken);
        }
        if (paymentInfo.hasKey("descriptor")) {
            wirecardPayment.setDescriptor(paymentInfo.getString("descriptor"));
        }
        if (paymentInfo.hasKey("orderID")) {
            wirecardPayment.setOrderNumber(paymentInfo.getString("orderID"));
        }

        ArrayList<Notification> notificationList = new ArrayList<>();
        if (paymentInfo.hasKey("notificationUrl")) {
            Notification SuccessNotif = new Notification();
            SuccessNotif.setTransactionState(TransactionState.SUCCESS.getValue());
            SuccessNotif.setUrl(paymentInfo.getString("notificationUrl"));
            // Notification InProgNotif = new
            // Notification(TransactionState.IN_PROGRES,paymentInfo.getString("notificationUrl"));
            // Notification RepeatedNotif = new
            // Notification(TransactionState.REPEATED,paymentInfo.getString("notificationUrl"));
            Notification FailureNotif = new Notification();
            FailureNotif.setTransactionState(TransactionState.FAILED.getValue());
            FailureNotif.setUrl(paymentInfo.getString("notificationUrl"));
            notificationList.add(SuccessNotif);
            // notificationList.add(InProgNotif);
            // notificationList.add(RepeatedNotif);
            notificationList.add(FailureNotif);
            Notifications notifications = new Notifications();
            notifications.setNotifications(notificationList);
            notifications.setFormat(Notifications.FORMAT_JSON);
            // wirecardPayment.setNotifications(notifications);
        }
        wirecardPayment.setRecurring(paymentInfo.getBoolean("setRecurring"));
        wirecardPayment.setAttempt3d(paymentInfo.getBoolean("setAttempt3d"));
        wirecardPayment
                .setRequireManualCardBrandSelection(paymentInfo.getBoolean("setRequireManualCardBrandSelection"));
        wirecardPayment.setAnimatedCardPayment(paymentInfo.getBoolean("setAnimatedCardPayment"));
        return wirecardPayment;
    }

    public GooglePayPayment createGPayPayment(ReadableMap paymentInfo) {
        GooglePayPayment googlePayPayment = new GooglePayPayment.Builder()
                .setShippingAddressRequired(paymentInfo.getBoolean("requireShippingAddress"))
                .setBillingAddressRequired(paymentInfo.getBoolean("requireBillingAddress"))
                .setEmailAddressRequired(paymentInfo.getBoolean("requireEmailAddress"))
                .setPhoneNumberRequired(paymentInfo.getBoolean("requirePhoneNumber"))
                // .setGooglePayUiTheme(true)
                // .setSupportedCardBrands(cardBrands)
                .setTransactionType(TransactionType.PURCHASE).setMerchantAccountId(paymentInfo.getString("merchantID"))
                .setSignature(paymentInfo.getString("signature")).setRequestId(paymentInfo.getString("requestID"))
                .setAmount(getAmount(paymentInfo.getString("amount"))).setCurrency(paymentInfo.getString("currency"))
                .build();
        googlePayPayment.setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION);
        return googlePayPayment;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == Client.PAYMENT_SDK_REQUEST_CODE) {
            Serializable paymentSDKResponse = data.getSerializableExtra(Client.EXTRA_PAYMENT_SDK_RESPONSE);
            if (paymentSDKResponse instanceof PaymentResponse) {
                responseHelper((PaymentResponse) paymentSDKResponse, this.getReactApplicationContext());
            }
        }
    }

    public void responseHelper(PaymentResponse paymentResponse, ReactApplicationContext context) {
        if (paymentResponse.getErrorMessage() != null) {
            WritableMap wresp = this.getWCRNPaymentResponse(paymentResponse);
            this.onPaymentFailure.invoke(paymentResponse.getErrorMessage(), wresp.getString("cardToken"),
                    wresp.getString("transactionState"), wresp.getString("transactionId"),
                    wresp.getString("requestId"));
        } else if (paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null) {
            WritableMap wresp = this.getWCRNPaymentResponse(paymentResponse);
            this.onPaymentSuccess.invoke(paymentResponse.getErrorMessage(), wresp.getString("cardToken"),
                    wresp.getString("cardBrand"), wresp.getString("transactionState"), wresp.getString("transactionId"),
                    wresp.getString("requestId"));
        }

    }

    private TransactionType getTransactionType(String name) {
        switch (name) {
        case "authorization":
            return TransactionType.AUTHORIZATION;
        case "authorization-only":
            return TransactionType.AUTHORIZATION_ONLY;
        case "capture-authorization":
            return TransactionType.CAPTURE_AUTHORIZATION;
        case "debit":
            return TransactionType.DEBIT;
        case "pending-debit":
            return TransactionType.PENDING_DEBIT;
        case "purchase":
            return TransactionType.PURCHASE;
        case "referenced-authorization":
            return TransactionType.REFERENCED_AUTHORIZATION;
        case "referenced-purchase":
            return TransactionType.REFERENCED_PURCHASE;
        case "tokenize":
            return TransactionType.TOKENIZE;
        default:
            return TransactionType.PURCHASE;
        }
    }

    private WritableMap getWCRNPaymentResponse(PaymentResponse paymentResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Response code: ").append(paymentResponse.getResponseCode());
        if (paymentResponse.getErrorMessage() != null) {
            sb.append(paymentResponse.getErrorMessage());
        }
        if (paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null) {
            sb.append("\n");
            for (Status status : paymentResponse.getPayment().getStatuses()) {
                sb.append(status.getCode());
                sb.append(":");
                sb.append(status.getDescription());
            }
        }
        String transactionId = "no payment";
        String requestId = "no request id";
        String cardToken = "no card token";
        String transactionState = " unknown transaction state";
        String cardBrand = "";
        if (paymentResponse.getPayment() != null) {
            Payment payment = paymentResponse.getPayment();
            transactionState = payment.getTransactionState();
            transactionId = payment.getTransactionId();
            requestId = payment.getRequestId();
            cardToken = payment.getCardToken().getTokenId();
            cardBrand = payment.getCard().getCardType();
        }
        WritableMap wresp = new WritableNativeMap();
        wresp.putString("transactionState", transactionState);
        wresp.putString("transactionId", transactionId);
        wresp.putString("status", sb.toString());
        wresp.putString("requestId", requestId);
        wresp.putString("cardToken", cardToken);
        wresp.putString("cardBrand", cardBrand);
        return wresp;
    }

    public static BigDecimal getAmount(String amount) {
        BigDecimal parsedAmount = new BigDecimal(amount);
        return parsedAmount;
    }

}