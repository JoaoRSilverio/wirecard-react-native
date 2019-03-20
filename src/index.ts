 // @ts-ignore
import {NativeModules} from 'react-native';

export const PAYMENT_METHODS = { card:"card",android_pay:"android_pay",paypal:"paypal"}

export interface IRNWirecardCcPayment {
    amount:string,
    paymentMethod:string,
    currency:string,
    requestID:string,
    merchantAccountID:string,
    signature:string,
    token?:string
}

export default class ReactNativeWirecard{
    public static initiateClient(onSuccess:()=>{},onFailure:()=>{},enviroment:string){
        NativeModules.RNWirecard.initiateClient(enviroment,onSuccess,onFailure);
    }

    public static pay( onSuccess:()=>{},onFailure:()=>{},payment:IRNWirecardCcPayment){
           NativeModules.RNWirecard.newPaymentRequest(
                payment,
                onSuccess,
                onFailure, 
            )
    }

}