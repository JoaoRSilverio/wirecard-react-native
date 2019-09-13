
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



RCT_EXPORT_METHOD(initiateClient:(NSString*) environment onSuccess:(RCTResponseSenderBlock)onSuccess onFailure:(RCTResponseSenderBlock)onFailure)
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
  -(WDECEnvironment) parseEnvironment:(NSString *) environment{
    if([environment isEqualToString:@"undefined"]){
        return WDECEnvironmentUndefined;
    }else if( [environment isEqualToString:@"singaporeProd"] ){
        return WDECEnvironmentSingaporePROD;
    }else if([environment isEqualToString:@"generalProd"]){
        return WDECEnvironmentPROD;
    }else if([environment isEqualToString:@"germanyProd"]){
        return WDECEnvironmentCEEPROD;
    }else if([environment isEqualToString:@"germanyTest"]){
        return WDECEnvironmentCEETEST;
    }else if([environment isEqualToString:@"torontoProd"]){
        return WDECEnvironmentTorontoPROD;
    }else if([environment isEqualToString:@"generalTest"]){
        return WDECEnvironmentTEST;
    }else if([environment isEqualToString:@"singaporeTest"]){
        return WDECEnvironmentSingaporeTEST;
    }else if([environment isEqualToString:@"torontoTest"]){
        return WDECEnvironmentTorontoTEST;
    }else if([environment isEqualToString:@"totalNumber"]){
        return WDECEnvironmentTotalNumber;
    }
    return WDECEnvironmentUndefined;
}
}

RCT_EXPORT_METHOD(newPaymentRequest:(NSDictionary *)payment
                  onPaymentSuccessfull:(RCTResponseSenderBlock) onSuccess
                  onPaymentFailed:(RCTResponseSenderBlock) onFailure){
    NSLog(@"OBJ creating payment instance");
    WDECPayment * wcpayment = [self createPayment:payment[@"paymentMethod"]];
    NSLog(@"OBJ creating populating fields");
    self.onPaymentFailed = onFailure;
    self.onPaymentSuccessfull = onSuccess;
    [wcpayment setAmount:(NSDecimalNumber* _Nullable)[NSDecimalNumber decimalNumberWithString: payment[@"amount"]]];
    [wcpayment setCurrency:(NSString * _Nullable) payment[@"currency"]];
    [wcpayment setTransactionType : WDECTransactionTypePurchase];
    [wcpayment setMerchantAccountID:(NSString * _Nullable) payment[@"merchantAccountID"]];
    [wcpayment setRequestID : (NSString * _Nullable) payment[@"requestID"]];
    [wcpayment setSignature:(NSString * _Nullable) payment[@"signature"]];
    if([payment objectForKey:@"token"] != nil){
        [wcpayment Card]
    }
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
    NSLog(@"OBJ creating signature ",wcpayment.merchantAccountID);
    */
    /*
     
    wcpayment.signature = [self
                         serverSideSignatureCalculationV2:requestTimestampStr
                         requestID:requestIDStr
                         merchantID:merchantAccount
                         transactionType:transactionTypeStr
                         amount:amountStr
                         currency:currencyStr
                         IPAddress:IPAddressStr
                         secretKey:@"9e0130f6-2e1e-4185-b0d5-dc69079c75cc"];
    */
   // NSLog(@"OBJ right before call %@",wcpayment);
    if(self.client){
        @weakify(self);
        [self.client makePayment:(WDECPayment*)wcpayment withCompletion:^(WDECPaymentResponse * _Nullable response,
                                                                        NSError * _Nullable error)
         {
             @strongify(self);
             NSLog(@"inside the block %@",wcpayment);
             NSMutableArray * events = [NSMutableArray array];
             if(error){
                 NSLog(@"error ",error.localizedDescription);
                 [events addObject:@"error while paying"];
                 self.onPaymentFailed(@[[NSNull null], events]);
             }else{
                 
                 [events addObject:@"Payment Successfull"];
                 self.onPaymentSuccessfull(@[[NSNull null], events]);
             }
         }];
    }else{
        NSLog(@"no client initiated");
    }
}

- (WDECPayment*)createPayment:(NSString *) title {
    WDECPayment *result = nil;
    if([title isEqualToString:@"card"]){
        result = [self createCardPayment];
    }else if([title isEqualToString:@"apple_pay"]){
        //result = [self createApplePayment];
    }else if([title isEqualToString:@"paypal"]){
        //result = [self createPayPalPayment];
    }else if([title isEqualToString:@"sepa"]){
        //result = [self createSepaPayment];
    }
    
    return result;
}

- (WDECPayment *) createCardPayment
{
    WDECCardPayment *payment = [WDECCardPayment new];
    return payment;
}
/// SERVER SIDE CODE
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
  
