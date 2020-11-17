#import "RNWirecard.h"
#import <WDeCom/WDeCom.h>
#import <WdeComCard/WDeComCard.h>
#import <WdeCom/WDECOrder.h>
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
    if([payment objectForKey:@"notifications"] != nil){
        wcpayment.notifications = [NSArray new];
        NSArray * notificationsURLS = payment[@"notifications"];
        for(int i = 0; i < notificationsURLS.count;i++){
            WDECNotification *notification = [WDECNotification new];
            notification.URL = [NSURL URLWithString:notificationsURLS[i]];
            NSLog(@"adding notification url %@",notificationsURLS[i]);
            wcpayment.notifications = [wcpayment.notifications arrayByAddingObject: notification];
        }
    }
    self.onPaymentFailed = onFailure;
    self.onPaymentSuccessfull = onSuccess;
    if(self.client){
        @weakify(self);
        [self.client makePayment:
            (WDECPayment*)wcpayment
            withCompletion:^(WDECPaymentResponse * _Nullable response,NSError * _Nullable error)
         {
             @strongify(self);
             NSString *transactionState= [self getTransactionStateString: response.transactionState];
             NSString * transactionId = response.transactionIdentifier ? response.transactionIdentifier : @"no transaction id ";
             NSString * requestId = response.requestID ? response.requestID : @"no request id";
             if(error){
                 self.onPaymentFailed(@[
                                        error.description,
                                        [NSNull null],
                                        transactionState,
                                        transactionId,
                                        requestId]);
             }else if(response){
                  WDECCardPaymentResponse *cardResponse = (WDECCardPaymentResponse *) response;
                 self.onPaymentSuccessfull(@[
                                             [NSNull null],
                                             cardResponse.cardToken.tokenID,
                                             transactionState,
                                             response.transactionIdentifier,
                                             response.requestID]);
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
    if(paymentData == nil){
        NSLog(@"no payment data from js");
    }
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
    WDECOrder *order = [WDECOrder new];
    if((NSString * _Nullable) [paymentData objectForKey:@"orderID"] != nil){
       [order setNumber:(NSString * _Nullable) paymentData[@"orderID"]]; 
    }
    if((NSString * _Nullable) [paymentData objectForKey:@"descriptor"] != nil) {
        [order setDescriptor:(NSString * _Nullable) paymentData[@"descriptor"]];
    }
    if((NSString * _Nullable)[paymentData objectForKey:@"orderID"]!= nil || (NSString * _Nullable)[paymentData objectForKey:@"descriptor"] != nil ){
     [cardPayment setOrder:(WDECOrder * _Nullable) order];
    }
    if([paymentData objectForKey:@"token"] != nil){
        WDECCardToken *cardToken = [WDECCardToken new];
        cardToken.tokenID =  paymentData[@"token"];
        cardPayment.token = cardToken;
        cardPayment.requireSecurityCode = true;
    }
    return cardPayment;
}
- (void) logPayment: (WDECPayment *) payment{
    NSLog(@"Payment -> ");
    NSLog(@"PAYMENT.AMOUNT %@", payment.amount);
    NSLog(@"PAYMENT.CURRENCY %@", payment.currency);
    // NSLog(@"PAYMENT.TRANSACTIONTYPE %@", payment.transactionType);
    NSLog(@"PAYMENT.MERCHACCOUNTID %@", payment.merchantAccountID);
    NSLog(@"PAYMENT.REQUESTID %@", payment.requestID);
    NSLog(@"PAYMENT.SIGNATURE %@", payment.signature);
    //NSLog(@"PAYMENT.TOKEN C%", payment.token);
}

@end
  
