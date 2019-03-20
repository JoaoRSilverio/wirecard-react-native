
#import "RNWirecard.h"
#import <WDeCom/WDeCom.h>
#import <WdeComCard/WDeComCard.h>
#import <CommonCrypto/CommonHMAC.h>
#import <libextobjc/EXTScope.h>
#import "NSDateFormatter+Utils.h"

NSString *const PMTitleCard = @"Card";


@implementation RNWirecard
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initiateClient:(NSString*) environment onSuccess:(RCTResponseSenderBlock)onSuccess onFailure:(RCTResponseSenderBlock)onFailure)
{
    NSLog(@"startin native module");
    @try{
        self.client = [[WDECClient alloc] initWithEnvironment:WDECEnvironmentTEST];
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

RCT_EXPORT_METHOD(newPaymentRequest:(NSString *)requestID
                  merchantAccount:(NSString *) merchantAccount
                  transactionType:(NSString *) transactionType
                  amount:(NSInteger *) amount
                  paymentMethod:(NSString *)  paymentMethod
                  currency:(NSString *) currency
                  onPaymentSuccessfull:(RCTResponseSenderBlock) onSuccess
                  onPaymentFailed:(RCTResponseSenderBlock) onFailure){
    NSLog(@"creating payment instance");
    WDECPayment *payment = [self createPayment:paymentMethod];
    NSLog(@"creating populating fields");
    [payment setAmount:(NSDecimalNumber* _Nullable)[NSDecimalNumber numberWithInteger: 10]];
    [payment setCurrency:(NSString * _Nullable) currency];
    [payment setTransactionType : WDECTransactionTypePurchase];
    [payment setMerchantAccountID:(NSString * _Nullable) merchantAccount];
    [payment setRequestID : (NSString * _Nullable) requestID];
    NSLog(@"converting fields to strings");
    NSString *requestIDStr = payment.requestID;
    NSString *transactionTypeStr = WDECTransactionTypeGetCode(payment.transactionType) ?: @"";
    NSString *amountStr = [payment.amount stringValue];
    NSString *currencyStr = payment.currency ?: @"";
    NSString *IPAddressStr = payment.IPAddress;
    
    
    
    
    NSDate *requestTimestamp = [NSDate date]; // UTC
    NSString *requestTimestampStr = [[NSDateFormatter timestampDateFormatter] stringFromDate:requestTimestamp];
    NSLog(@"creating signature %@",requestTimestampStr);
    
    payment.signature = [self
                         serverSideSignatureCalculationV2:requestTimestampStr
                         requestID:requestIDStr
                         merchantID:merchantAccount
                         transactionType:transactionTypeStr
                         amount:amountStr
                         currency:currencyStr
                         IPAddress:IPAddressStr
                         secretKey:@"9e0130f6-2e1e-4185-b0d5-dc69079c75cc"];
    NSLog(@"%@",payment);
    if(self.client){
        @weakify(self);
        [self.client makePayment:(WDECPayment*)payment withCompletion:^(WDECPaymentResponse * _Nullable response,
                                                                        NSError * _Nullable error)
         {
             @strongify(self);
             NSLog(@"inside the block %@",payment);
             NSMutableArray * events = [NSMutableArray array];
             if(error){
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
    if([title isEqualToString:title]){
        result = [self createCardPayment];
    }
    
    return result;
}

- (WDECPayment *) createCardPayment
{
    WDECCardPayment *payment = [WDECCardPayment new];
    
    return payment;
}
/// SERVER SIDE CODE
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
@end
  
