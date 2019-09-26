#import "RNWirecard.h"
#import <WDeCom/WDeCom.h>
#import <WdeComCard/WDeComCard.h>
#import <CommonCrypto/CommonHMAC.h>
#import <libextobjc/EXTScope.h>
#import "NSDateFormatter+Utils.h"

NSString *const PMTitleCard = @"Card";
RCTResponseSenderBlock onPaymentSuccess;
RCTResponseSenderBlock onPaymentFail;
@implementation RNWirecard
RCT_EXPORT_MODULE()



RCT_EXPORT_METHOD(initiateClient:(NSString*) environment onSuccess:(RCTResponseSenderBlock)onSuccess 
onFailure:(RCTResponseSenderBlock)onFailure
)
{
    NSLog(@"OBJ startin native module");
    @try{
        self.client = [[WDECClient alloc] initWithEnvironment:[self parseEnvironment:environment]];
        onSuccess(@[[NSNull null]]);
    }
    @catch(NSException * exception){
       onFailure(@[[NSNull null]]);
    }
    @finally{
        
    }
    
    
}
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

  -(WDECEnvironment) parseEnvironment:(NSString *) environment{
    if([environment isEqualToString:@"undefined"]){
        NSLog(@"setting WDECEnvironmentUndefined");
        return WDECEnvironmentUndefined;
    }else if( [environment isEqualToString:@"singaporeProd"] ){
        return WDECEnvironmentSingaporePROD;
    }else if([environment isEqualToString:@"generalProd"]){
        return WDECEnvironmentPROD;
    }else if([environment isEqualToString:@"germanyProd"]){
        NSLog(@"setting WDECEnvironmentCEEPROD");
        return WDECEnvironmentCEEPROD;
    }else if([environment isEqualToString:@"germanyTest"]){
        NSLog(@"setting WDECEnvironmentCEETEST");
        return WDECEnvironmentCEETEST;
    }else if([environment isEqualToString:@"torontoProd"]){
        return WDECEnvironmentTorontoPROD;
    }else if([environment isEqualToString:@"generalTest"]){
        NSLog(@"setting WDECEnvironmentTEST");
        return WDECEnvironmentTEST;
    }else if([environment isEqualToString:@"singaporeTest"]){
        return WDECEnvironmentSingaporeTEST;
    }else if([environment isEqualToString:@"torontoTest"]){
        return WDECEnvironmentTorontoTEST;
    }else if([environment isEqualToString:@"totalNumber"]){
        return WDECEnvironmentTotalNumber;
    } else{
    return WDECEnvironmentUndefined;
    }
}


RCT_EXPORT_METHOD(newPaymentRequest:(NSDictionary *)payment
                  onPaymentSuccessfull:(RCTResponseSenderBlock) onSuccess
                  onPaymentFailed:(RCTResponseSenderBlock) onFailure
                  ){
    WDECPayment * wcpayment = [self createPayment:payment[@"paymentMethod"] paymentData:payment];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ss'Z'";
    NSDate *timeStamp = [formatter dateFromString:payment[@"requestTimeStamp"]];
    [wcpayment setRequestTimestamp: timeStamp];
    if([payment objectForKey:@"notificationUrl"] != nil){
        WDECNotification *notification = [WDECNotification new];
        notification.URL = [NSURL URLWithString:payment[@"notificationUrl"]];
        wcpayment.notifications = @[notification];
    }
    self.onPaymentFailed = onFailure;
    self.onPaymentSuccessfull = onSuccess;
    /*
    NSLog(@"OBJ converting fields to strings");
    NSString *requestIDStr = wcpayment.requestID;
    NSString *transactionTypeStr = WDECTransactionTypeGetCode(wcpayment.transactionType) ?: @"";
    NSString *amountStr = [wcpayment.amount stringValue];
    NSString *currencyStr = wcpayment.currency ?: @"";
    NSString *IPAddressStr = wcpayment.IPAddress;
    NSString *merchantAccount = wcpayment.merchantAccountID;
    NSDate *requestTimestamp = [NSDate date]; // UTC
    NSString *requestTimestampStr = [[NSDateFormatter timestampDateFormatter] stringFromDate:requestTimestamp];
    NSLog(@"OBJ creating signature account id %@",wcpayment.merchantAccountID);
    NSLog(@"OBJ creating signature account secret %@",@"9e0130f6-2e1e-4185-b0d5-dc69079c75cc");
    
     
    wcpayment.signature = [self
                         serverSideSignatureCalculationV2:requestTimestampStr
                         requestID:requestIDStr
                         merchantID:merchantAccount
                         transactionType:transactionTypeStr
                         amount:amountStr
                         currency:currencyStr
                         IPAddress:IPAddressStr
                         secretKey:@"b3b131ad-ea7e-48bc-9e71-78d0c6ea579d"];
    */
   // NSLog(@"OBJ right before call %@",wcpayment);
    if(self.client){
        @weakify(self);
        [self.client makePayment:(WDECPayment*)wcpayment withCompletion:^(WDECPaymentResponse * _Nullable response,
                                                                        NSError * _Nullable error)
         {
             @strongify(self);
             NSMutableArray * events = [NSMutableArray array];
             if(error){
                 [events addObject:@"error while paying"];
                 self.onPaymentFailed(@[[NSNull null], events]);
             }else if(response){
                 NSString *transactionState= [self getTransactionStateString: response.transactionState];
                 [events addObject:response.statusMessage];
                 self.onPaymentSuccessfull(@[[NSNull null], events, transactionState]);
             }
         }];
    }else{
        NSLog(@"no client initiated");
    }
}
- (NSString *) getTransactionStateString:(WDECTransactionState) state {
    switch ((int)state) {
            case (int)WDECTransactionStateFailed:
            return @"WDECTransactionStateFailed";
            break;
            case (int)WDECTransactionStateSuccess:
            return @"WDECTransactionStateSuccess";
            break;
            case (int)WDECTransactionStateInProgress:
            return @"WDECTransactionStateInProgress";
            break;
            case (int)WDECTransactionStateRepeated:
            return @"WDECTransactionStateRepeated";
            break;
            case (int)WDECTransactionStateTotalNumber:
            return @"WDECTransactionStateTotalNumber";
            break;
            case (int)WDECTransactionStateUndefined:
            return @"WDECTransactionStateUndefined";
            break;
        default:
            return @"WDECTransactionStateUndefined";
            break;
    }
}
- (WDECPayment*)createPayment:(NSString *) title paymentData:(NSDictionary *) paymentData {
    WDECPayment *result = nil;
    if([title isEqualToString:@"card"]){
        result = [self createCardPayment: paymentData];
    }else if([title isEqualToString:@"apple_pay"]){
        //result = [self createApplePayment];
    }else if([title isEqualToString:@"paypal"]){
        //result = [self createPayPalPayment];
    }else if([title isEqualToString:@"sepa"]){
        //result = [self createSepaPayment];
    }
    
    return result;
}

- (WDECPayment *) createCardPayment:(NSDictionary *) paymentData
{
    WDECCardPayment *cardPayment = [WDECCardPayment new];
    [cardPayment setAmount:(NSDecimalNumber* _Nullable)[NSDecimalNumber decimalNumberWithString: paymentData[@"amount"]]];
    [cardPayment setCurrency:(NSString * _Nullable) paymentData[@"currency"]];
    [cardPayment setTransactionType : WDECTransactionTypePurchase];
    [cardPayment setMerchantAccountID:(NSString * _Nullable) paymentData[@"merchantID"]];
    [cardPayment setRequestID : (NSString * _Nullable) paymentData[@"requestID"]];
    [cardPayment setSignature:(NSString * _Nullable) paymentData[@"signature"]];
    if([paymentData objectForKey:@"token"] != nil){
        WDECCardToken *cardToken = [WDECCardToken new];
        cardToken.tokenID =  paymentData[@"token"];
        cardPayment.token = cardToken;
        cardPayment.requireSecurityCode = true;
    }
    return cardPayment;
}
/// SIMULATED SERVER SIDE CODE
/*
- (NSString *)serverSideSignatureCalculationV2:(NSString *)requestTimestamp
                                     requestID:(NSString *)requestID
                                    merchantID:(NSString *)merchantID
                               transactionType:(NSString *)transactionType
                                        amount:(NSString *)amount
                                      currency:(NSString *)currency
                                     IPAddress:(NSString *)IPAddress
                                     secretKey:(NSString *)secretKey
{
    NSLog(@"starting server side Calculations");
    NSMutableArray<NSString *> *params = [NSMutableArray arrayWithCapacity:9];
    [params addObject:@"HS256"];
    [params addObject:[NSString stringWithFormat:@"request_time_stamp=%@", requestTimestamp]];
    NSLog(@"payment object info=%@ %@ %@ %@ %@", requestTimestamp,merchantID , transactionType , amount , currency);
    [params addObject:[NSString stringWithFormat:@"merchant_account_id=%@", merchantID]];
    [params addObject:[NSString stringWithFormat:@"request_id=%@", requestID]];
    [params addObject:[NSString stringWithFormat:@"transaction_type=%@", transactionType]];
    [params addObject:[NSString stringWithFormat:@"requested_amount=%@", amount]];
    [params addObject:[NSString stringWithFormat:@"requested_amount_currency=%@", currency]];
    if (IPAddress) {
        [params addObject:[NSString stringWithFormat:@"ip_address=%@", requestTimestamp]];
    }
    NSString *payload = [params componentsJoinedByString:@"\n"];
    
    NSString *payloadBase64 = [[payload dataUsingEncoding:NSUTF8StringEncoding] base64EncodedStringWithOptions:0];
    NSString *hmacBase64    = [self HMAC256WithKey:secretKey data:payload];
    
    NSString *signature = [@[payloadBase64, hmacBase64] componentsJoinedByString:@"."];
    return signature;
}
- (nullable NSString *)HMAC256WithKey:(nonnull NSString *)key data:(NSString *) data {
    if (!key) {
        return nil;
    }
    
    NSData *rawData = [data dataUsingEncoding:NSUTF8StringEncoding];
    NSData *rawKey  = [key  dataUsingEncoding:NSUTF8StringEncoding];
    
    NSMutableData *hash = [NSMutableData dataWithLength:CC_SHA256_DIGEST_LENGTH ];
    CCHmac(kCCHmacAlgSHA256, rawKey.bytes, rawKey.length, rawData.bytes, rawData.length, hash.mutableBytes);
    
    NSData *rawHMAC = [NSData dataWithData:hash];
    
    NSString *HMAC  = [rawHMAC base64EncodedStringWithOptions:0];
    return HMAC;
}
*/

@end
  
