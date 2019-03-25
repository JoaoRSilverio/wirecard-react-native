
package com.rnwirecard;

import com.facebook.common.activitylistener.BaseActivityListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.telecom.Call;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
/*
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
*/
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
//import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

import android.util.Base64;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import com.wirecard.ecom.Client;
import com.wirecard.ecom.model.Payment;
import com.wirecard.ecom.model.Status;
import com.wirecard.ecom.model.TransactionType;
import com.wirecard.ecom.model.out.PaymentResponse;
import com.wirecard.ecom.card.model.CardPayment;




public class RNWirecardModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String ENCRYPTION_ALGORITHM = "HS256";
    private static final String UTF_8 = "UTF-8";
    public final static String URL_EE_TEST = "https://api-test.wirecard.com";
    public final static int REQUEST_TIMEOUT = 30;
    public Callback onPaymentFailure;
    public Callback onPaymentSuccess;
    public Client wirecardClient;
    public CardPayment wirecardPayment;
    private final ReactApplicationContext reactContext;

    public RNWirecardModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @ReactMethod
    public void initiateClient(String enviroment,Callback onSuccess,Callback onFailure){
        Log.i("JAVA", "initiating client");
        wirecardClient = new Client(this.getCurrentActivity(),URL_EE_TEST, REQUEST_TIMEOUT);
        onSuccess.invoke();
    }
    @ReactMethod
    public void newPaymentRequest(
            ReadableMap payment,
            Callback onPaymentAccepted,
            Callback onPaymentRejected
    ) {
        wirecardPayment =  getAppropriatePayment(payment);
        /// this wont be present in final version
        //final String secret = "9e0130f6-2e1e-4185-b0d5-dc69079c75cc"; //     // test data from 3D Manual Brand Card Recognition
        this.onPaymentFailure = onPaymentRejected;
        this.onPaymentSuccess = onPaymentAccepted;
        wirecardClient.startPayment(wirecardPayment);

    }
    public CardPayment getAppropriatePayment(ReadableMap paymentInfo ){
        BigDecimal amount = getAmount(paymentInfo.getString("amount"));
        switch (paymentInfo.getString("paymentMethod")) {
            case "card":
                //Log.i("JAVA", "processing card");
                wirecardPayment = new CardPayment.Builder()
                        .setSignature(paymentInfo.getString("signature"))
                        .setMerchantAccountId(paymentInfo.getString("merchantAccountID"))
                        .setTransactionType(TransactionType.PURCHASE)
                        .setRequestId(paymentInfo.getString("requestID"))
                        .setAmount(amount)
                        .setCurrency(paymentInfo.getString("currency"))
                        .build();
                wirecardPayment.setAnimatedCardPayment(true);
                break;
            default:
                Log.i("JAVA", "processing card by default");
                wirecardPayment = new CardPayment.Builder()
                        .setSignature(paymentInfo.getString("signature"))
                        .setMerchantAccountId(paymentInfo.getString("merchantAccountID"))
                        .setRequestId(paymentInfo.getString("requestID"))
                        .setAmount(amount)
                        .setTransactionType(TransactionType.PURCHASE)
                        .setCurrency(paymentInfo.getString("currency"))
                        .build();
                wirecardPayment.setAnimatedCardPayment(true);
                break;
        }
        return  wirecardPayment;
    }
    public BigDecimal getAmount(String amount){
        BigDecimal parsedAmount =new BigDecimal(amount).setScale(0, RoundingMode.HALF_EVEN);
        return parsedAmount;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode == Client.PAYMENT_SDK_REQUEST_CODE){
            Serializable paymentSDKResponse = data.getSerializableExtra(Client.EXTRA_PAYMENT_SDK_RESPONSE);
            if(paymentSDKResponse instanceof PaymentResponse){
                responseHelper((PaymentResponse) paymentSDKResponse,this.getReactApplicationContext());
            }
        }
    }

    public void responseHelper(PaymentResponse paymentResponse,ReactApplicationContext context){
        if(paymentResponse.getErrorMessage()  != null){
            this.onPaymentFailure.invoke();
        }
        if(paymentResponse.getPayment() != null && paymentResponse.getPayment().getStatuses() != null){
            this.onPaymentSuccess.invoke();
        }
    }
    @Override
    public void onNewIntent(Intent intent) {

    }
    @Override
    public String getName() {
        return "RNWirecard";
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