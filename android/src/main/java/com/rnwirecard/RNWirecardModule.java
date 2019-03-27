
package com.rnwirecard;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;

/*
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
*/
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;



import com.wirecard.ecom.Client;
import com.wirecard.ecom.model.CardToken;
import com.wirecard.ecom.model.Payment;
import com.wirecard.ecom.model.TransactionType;
import com.wirecard.ecom.model.out.PaymentResponse;
import com.wirecard.ecom.card.model.CardPayment;

// import com.wirecard.ecom.paypal.model.PayPalPayment;
// import com.wirecard.ecom.sepa.model.SepaPayment;
// import com.wirecard.ecom.zapp.model.ZappPayment;




public class RNWirecardModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    //private static final String ENCRYPTION_ALGORITHM = "HS256";
    //private static final String UTF_8 = "UTF-8";
    public final static int REQUEST_TIMEOUT = 30;
    public Callback onPaymentFailure;
    public Callback onPaymentSuccess;
    public Client wirecardClient;
    //public CardPayment wirecardPayment;
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
    public void initiateClient(String environment,Callback onSuccess,Callback onFailure){
        Log.i("wirecard-react-native", " trying to initiate client with endpoint: " + environment);
        try{
            wirecardClient = new Client(this.getCurrentActivity(),environment, REQUEST_TIMEOUT);
            Log.i("wirecard-react-native", "initiating client with endpoint: " + environment);
            onSuccess.invoke();
        }catch (Exception e){
            Log.i("wirecard-react-native", "client failed to initiate with endpoint: " + environment);
            Log.i("wirecard-react-native", e.getErrorMessage());

            onFailure.invoke();
        }
    }
    @ReactMethod
    public void testBridge(String received){
        Log.i("wirecard-react-native",received);
    }
    @ReactMethod
    public void newPaymentRequest(ReadableMap payment,Callback onPaymentAccepted,Callback onPaymentRejected) {
        this.onPaymentFailure = onPaymentRejected;
        this.onPaymentSuccess = onPaymentAccepted;
        this.startRequestedPayment(payment);
    }

    public void startRequestedPayment(ReadableMap paymentInfo ){
        switch (paymentInfo.getString("paymentMethod")) {
            case "card":
                Log.i("wirecard-react-native", "processing Credit Card payment");
                CardPayment cardPayment = this.createCardPayment(paymentInfo);
                wirecardClient.startPayment(cardPayment);
                break;
            case "paypal":
                Log.i("wirecard-react-native", "processing PayPal payment");
                //PayPalPayment payPalPayment = this.createPayPalPayment(paymentInfo);
                //wirecardClient.startPayment(payPalPayment);
            case "sepa":
                Log.i("wirecard-react-native", "processing SEPA payment");
                // SepaPayment sepaPayment = this.createSepaPayment(paymentInfo);
                // wirecardClient.startPayment(sepaPayment);
            case "zapp":
                Log.i("wirecard-react-native", "processing Zapp payment");
                // ZappPayment zappPayment = this.createZappPayment(paymentInfo);
                // wirecardClient.startPayment(zappPayment);
            default:
                Log.i("wirecard-react-native", "processing Credit Card by default");
                CardPayment defaultCardPayment = this.createCardPayment(paymentInfo);
                break;
        }
    }

    public CardPayment createCardPayment(ReadableMap paymentInfo){
        CardPayment wirecardPayment = new CardPayment.Builder()
                .setSignature(paymentInfo.getString("signature"))
                .setMerchantAccountId(paymentInfo.getString("merchantAccountID"))
                .setTransactionType(TransactionType.PURCHASE)
                .setRequestId(paymentInfo.getString("requestID"))
                .setAmount(getAmount(paymentInfo.getString("amount")))
                .setCurrency(paymentInfo.getString("currency"))
                .build();
        if(paymentInfo.hasKey("token")){
            String token = paymentInfo.getString("token");
            String maskedAccountNumber = paymentInfo.getString("maskedAccountNumber");
            CardToken cardToken = new CardToken();
            cardToken.setMaskedAccountNumber(maskedAccountNumber);
            cardToken.setTokenId(token);
            wirecardPayment.setCardToken(cardToken);
        }
        wirecardPayment.setAnimatedCardPayment(true);
        return wirecardPayment;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if(requestCode == Client.PAYMENT_SDK_REQUEST_CODE){
            Serializable paymentSDKResponse = data.getSerializableExtra(Client.EXTRA_PAYMENT_SDK_RESPONSE);
            if(paymentSDKResponse instanceof PaymentResponse){
                responseHelper((PaymentResponse) paymentSDKResponse,this.getReactApplicationContext());
            }
        }
    }

    public void responseHelper(PaymentResponse paymentResponse,ReactApplicationContext context){
        if(paymentResponse.getErrorMessage()  != null){
            Log.i("wirecard-react-native", "payment failed:\n" + paymentResponse.getErrorMessage() );

            this.onPaymentFailure.invoke();
        }
        if(paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null){
            this.onPaymentSuccess.invoke();
        }
    }



    public static  BigDecimal getAmount(String amount){
        BigDecimal parsedAmount =new BigDecimal(amount).setScale(0, RoundingMode.HALF_EVEN);
        return parsedAmount;
    }










    /**
     *  ALL THIS METHODS ARE ONLY FOR TESTING IMPLEMENTATION
     *
     *
     /*

     public static String generateTimestamp() {
     LocalDateTime current = LocalDateTime.now(ZoneOffset.UTC);
     DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
     String timestamp = current.format(formatter);
     return timestamp.substring(0,timestamp.length() - 4) + "Z";
     }


     public String generateSignatureV2(String timestamp,String requestID,String mercAccount) {
     Map<String, String> map = new HashMap<>();
     map.put("request_time_stamp", timestamp); // yyyy-MM-dd'T'HH:mm:ssXXX
     map.put("request_id", requestID);
     map.put("merchant_account_id", mercAccount);
     map.put("transaction_type", "purchase");
     map.put("requested_amount", Integer.toString(10));
     map.put("requested_amount_currency", "EUR");

     return toHmacSha256(map, "9e0130f6-2e1e-4185-b0d5-dc69079c75cc");
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