
#if __has_include("RCTBridgeModule.h")
#import "RCTBridge.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import <Foundation/Foundation.h>
@class WDECClient;
@class WDECPayment;


@interface RNWirecard : NSObject<RCTBridgeModule>
@property (strong,nullable,nonatomic) WDECClient *client;
@property (strong,nullable,nonatomic) RCTResponseSenderBlock onPaymentSuccessfull;
@property (strong,nullable,nonatomic) RCTResponseSenderBlock onPaymentFailed;

-(void)initiateClient:(NSString*)environment onSuccess:(RCTResponseSenderBlock)onSuccess onFailure:(RCTResponseSenderBlock)onFailure ;
-(void)newPaymentRequest:
(NSString *)requestID
         merchantAccount:(NSString *) merchantAccount
         transactionType:(NSString *) transactionType
                  amount:(NSInteger *) amount
           paymentMethod:(NSString *) paymentMethod
                currency:(NSString *) currency
    onPaymentSuccessfull:(RCTResponseSenderBlock) onSuccess
         onPaymentFailed:(RCTResponseSenderBlock) onFailure
;

@end

  
