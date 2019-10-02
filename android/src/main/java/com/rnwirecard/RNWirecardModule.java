
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


import com.wirecard.ecom.model.Status;
import com.wirecard.ecom.Client;
import com.wirecard.ecom.model.Notification;
// import com.wirecard.ecom.model.Notifications;
import com.wirecard.ecom.model.CardToken;
import com.wirecard.ecom.model.Payment;
import com.wirecard.ecom.model.TransactionType;
import com.wirecard.ecom.model.TransactionState;
import com.wirecard.ecom.model.out.PaymentResponse;
import com.wirecard.ecom.ResponseCode;
import com.wirecard.ecom.card.model.CardPayment;

public class RNWirecardModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    //private static final String ENCRYPTION_ALGORITHM = "HS256";
    //private static final String UTF_8 = "UTF-8";
     //public CardPayment wirecardPayment;
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
    public void onNewIntent(Intent intent){

    }

    @ReactMethod
    public void initiateClient(
        String environment,
        Callback onSuccess,
        Callback onFailure){
        Log.i(
            "wirecard-react-native",
            " trying to initiate client with endpoint: " + environment);
        try{
            wirecardClient = new Client(this.getCurrentActivity(),environment, REQUEST_TIMEOUT);
            Log.i
            ("wirecard-react-native", 
            "initiating client with endpoint: " + environment);
            onSuccess.invoke();
        }catch (Exception e){
            Log.i(
                "wirecard-react-native",
                "client failed to initiate with endpoint: " + environment);
            Log.i("wirecard-react-native", e.getMessage());

            onFailure.invoke();
        }
    }
    @ReactMethod
    public void testBridge(String received){
        Log.i("wirecard-react-native",received);
    }
    @ReactMethod
    public void newPaymentRequest(
        ReadableMap payment,
        Callback onPaymentAccepted,
        Callback onPaymentRejected) {
        this.onPaymentFailure = onPaymentRejected;
        this.onPaymentSuccess = onPaymentAccepted;
        this.startRequestedPayment(payment);
    }

    public void startRequestedPayment(ReadableMap paymentInfo ){
        Log.i("wirecard-react-native","initiating actual transaction");
        switch (paymentInfo.getString("paymentMethod")) {
            case "card":
                CardPayment cardPayment = this.createCardPayment(paymentInfo);
                wirecardClient.startPayment(cardPayment);
                break;
            case "paypal":
                //PayPalPayment payPalPayment = this.createPayPalPayment(paymentInfo);
                //wirecardClient.startPayment(payPalPayment);
            case "sepa":
                // SepaPayment sepaPayment = this.createSepaPayment(paymentInfo);
                // wirecardClient.startPayment(sepaPayment);
            case "zapp":
                // ZappPayment zappPayment = this.createZappPayment(paymentInfo);
                // wirecardClient.startPayment(zappPayment);
            default:
                CardPayment defaultCardPayment = this.createCardPayment(paymentInfo);
                break;
        }
    }
    //.setTransactionType(paymentInfo.getString("transactionType"))
    public CardPayment createCardPayment(ReadableMap paymentInfo){
       
        /*
        String signature = this.generateSignatureV2(
            paymentInfo.getString("requestTimeStamp"), paymentInfo.getString("requestID"),
            paymentInfo.getString("merchantID"), paymentInfo.getString("amount"));
        */
        CardPayment wirecardPayment = new CardPayment.Builder()
                .setSignature(paymentInfo.getString("signature"))
                .setMerchantAccountId(paymentInfo.getString("merchantID"))
                .setTransactionType(TransactionType.PURCHASE)
                .setRequestId(paymentInfo.getString("requestID"))
                .setAmount(getAmount(paymentInfo.getString("amount")))
                .setCurrency(paymentInfo.getString("currency"))
                .build();
        Log.i("wirecard-react-native",paymentInfo.getString("currency"));
        if(paymentInfo.hasKey("token")){
            String token = paymentInfo.getString("token");
            String maskedAccountNumber = paymentInfo.getString("maskedAccountNumber");
            CardToken cardToken = new CardToken();
            cardToken.setMaskedAccountNumber(maskedAccountNumber);
            cardToken.setTokenId(token);
            wirecardPayment.setCardToken(cardToken);
        }
        // ArrayList<Notification> notificationList = new ArrayList<>();
        /*
        if(paymentInfo.hasKey("notificationUrl")) {
            Log.i("wirecard-react-native","has notification, url below");
            Log.i("wirecard-react-native",paymentInfo.getString("notificationUrl"));
            Notification SuccessNotif = new Notification();
            SuccessNotif.setTransactionState(TransactionState.SUCCESS.getValue());
            SuccessNotif.setUrl(paymentInfo.getString("notificationUrl"));
            //Notification InProgNotif = new Notification(TransactionState.IN_PROGRES,paymentInfo.getString("notificationUrl"));
            //Notification RepeatedNotif = new Notification(TransactionState.REPEATED,paymentInfo.getString("notificationUrl"));
            Notification FailureNotif = new Notification();
            FailureNotif.setTransactionState(TransactionState.FAILED.getValue());
            FailureNotif.setUrl(paymentInfo.getString("notificationUrl"));
            notificationList.add(SuccessNotif);
            //notificationList.add(InProgNotif);
            //notificationList.add(RepeatedNotif);
            notificationList.add(FailureNotif);
            Notifications notifications = new Notifications();
            notifications.setNotifications(notificationList);
            notifications.setFormat(Notifications.FORMAT_JSON);
            // wirecardPayment.setNotifications(notifications);
        }
        */
        Log.i("wirecard-react-native","server sent:");
        Log.i("wirecard-react-native",paymentInfo.getString("signature"));
        Log.i("wirecard-react-native","generated:");
        //Log.i("wirecard-react-native",signature);
        Log.i("wirecard-react-native",paymentInfo.getString("amount"));
        Log.i("wirecard-react-native",paymentInfo.getString("currency"));
        Log.i("wirecard-react-native","lauching screen");

        wirecardPayment.setRecurring(false);
        wirecardPayment.setAttempt3d(false);
        wirecardPayment.setRequireManualCardBrandSelection(false);
        wirecardPayment.setAnimatedCardPayment(true);
        return wirecardPayment;
    }

    @Override
    public void onActivityResult(
        Activity activity, 
        int requestCode, 
        int resultCode, 
        Intent data) {
        if(requestCode == Client.PAYMENT_SDK_REQUEST_CODE){
            Serializable paymentSDKResponse = 
            data.getSerializableExtra(Client.EXTRA_PAYMENT_SDK_RESPONSE);
            if(paymentSDKResponse instanceof PaymentResponse){
                responseHelper(
                    (PaymentResponse) paymentSDKResponse,
                    this.getReactApplicationContext());
            }
        }
    }

    public void responseHelper(
        PaymentResponse paymentResponse,
        ReactApplicationContext context) {
        if (paymentResponse.getErrorMessage() != null) {
            WritableMap wresp = this.getWCRNPaymentResponse(paymentResponse);
            this.onPaymentFailure.invoke(
                paymentResponse.getErrorMessage(),
                wresp.getString("cardToken"),
                wresp.getString("transactionState"),
                wresp.getString("transactionId"),
                wresp.getString("requestId"));
        } else if (paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null) {
            WritableMap wresp = this.getWCRNPaymentResponse(paymentResponse);
            this.onPaymentSuccess.invoke(
                paymentResponse.getErrorMessage(),
                wresp.getString("cardToken"),
                wresp.getString("transactionState"),
                wresp.getString("transactionId"),
                wresp.getString("requestId"));
        }

    }
   
    private WritableMap getWCRNPaymentResponse(PaymentResponse paymentResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("Response code: ").append(paymentResponse.getResponseCode());
        if(paymentResponse.getErrorMessage() != null) {
            sb.append(paymentResponse.getErrorMessage());
        }
        if(paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null) {
            sb.append("\n");
            for(Status status: paymentResponse.getPayment().getStatuses()) {
                sb.append(status.getCode());
                sb.append(":");
                sb.append(status.getDescription());
            }
        }
        String transactionId = "no payment";
        String requestId = "no request id";
        String cardToken = "no card token";
        String transactionState = " unknown transaction state";
        if(paymentResponse.getPayment() != null) {
            Payment payment = paymentResponse.getPayment();
            transactionState = payment.getTransactionState();
            transactionId = payment.getTransactionId();
            requestId = payment.getRequestId();
            cardToken = payment.getCardToken().getTokenId();
        }
        WritableMap wresp = new WritableNativeMap();
        wresp.putString("transactionState",transactionState);
        wresp.putString("transactionId",transactionId);
        wresp.putString("status", sb.toString());
        wresp.putString("requestId",requestId);
        wresp.putString("cardToken", cardToken);
            return wresp;
    }
    public static  BigDecimal getAmount(String amount){
        BigDecimal parsedAmount =new BigDecimal(amount).setScale(0, RoundingMode.HALF_EVEN);
        return parsedAmount;
    }


    /**
     *  ALL THIS METHODS ARE ONLY FOR TESTING IMPLEMENTATION
     *
     *
     */
    /*
     public static String generateTimestamp() {
     LocalDateTime current = LocalDateTime.now(ZoneOffset.UTC);
     DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
     String timestamp = current.format(formatter);
     return timestamp.substring(0,timestamp.length() - 4) + "Z";
     }


     public String generateSignatureV2(
         String timestamp,
         String requestID,
         String mercAccount,
         String amount
         ) {
     Map<String, String> map = new HashMap<>();
     map.put("request_time_stamp", timestamp); // yyyy-MM-dd'T'HH:mm:ssXXX
     map.put("request_id", requestID);
     map.put("merchant_account_id", mercAccount);
     map.put("transaction_type", "purchase");
     map.put("requested_amount", amount);
     map.put("requested_amount_currency", "EUR");

     return toHmacSha256(map, "b3b131ad-ea7e-48bc-9e71-78d0c6ea579d");
     }

     private String toHmacSha256(Map<String, String> fields, String secret) {
     Charset charset = Charset.forName("UTF-8");

     StringBuilder builder = new StringBuilder("HS256\n");
     for (Map.Entry<String, String> field : fields.entrySet()) {
     builder.append(field.getKey());
     builder.append("=");
     builder.append(field.getValue());
     builder.append("\n");
     }
     byte[] data = builder.toString().getBytes(charset);
     byte[] key = secret.getBytes(charset);
     byte[] sign = sign(key, data);
     return new StringBuilder()
     .append(Base64.encodeToString(data,Base64.NO_WRAP))
     .append(".")
     .append(Base64.encodeToString(sign,Base64.NO_WRAP))
     .toString();
     }

     public byte[] sign(byte[] key, byte[] data) {
     try {
     Mac mac = Mac.getInstance("HmacSHA256");
     mac.init(new SecretKeySpec(key, "HmacSHA256"));
     byte[] signature = mac.doFinal(data);
     return signature;
     } catch (NoSuchAlgorithmException | InvalidKeyException e) {
     return null;
     }
     }
     */

}