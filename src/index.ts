 // @ts-ignore
import {NativeModules} from 'react-native';
var RNWirecard = NativeModules.RNWirecard;


export const TRANSACTION_TYPES ={
    AUTHORIZATION:'authorization',
    AUTHORIZATION_ONLY:'authorization-only',
    DEBIT:'debit',
    PENDING_DEBIT:'pending-debit',
    PURCHASE:'purchase',
    TOKENIZE:'tokenize',
}

export const PAYMENT_METHODS = { 
    // #TODO add other payment methods
    APPLE_PAY:{
        paymentMethod:"apple_pay",
        transactionType:{
            authorization:TRANSACTION_TYPES.AUTHORIZATION,
            purchase:TRANSACTION_TYPES.PURCHASE
        }
    },
    CARD:{
        paymentMethod:"card",
        transactionType:{
            authorization:TRANSACTION_TYPES.AUTHORIZATION,
            authorization_only:TRANSACTION_TYPES.AUTHORIZATION_ONLY,
            purchase:TRANSACTION_TYPES.PURCHASE,
            tokenize:TRANSACTION_TYPES.TOKENIZE
        }
    },
    PAYPAL:{
        paymentMethod:"paypal",
        transactionType:{
            authorization:TRANSACTION_TYPES.AUTHORIZATION,
            authorization_only:TRANSACTION_TYPES.AUTHORIZATION_ONLY,
            debit:TRANSACTION_TYPES.DEBIT
        }
    },
    SEPA:{
        paymentMethod:'sepa',
        transactionType:{
            authorization:TRANSACTION_TYPES.AUTHORIZATION,
            pendingDebit:TRANSACTION_TYPES.PENDING_DEBIT
        }
    }
}

export const ANDROID_ENVIRONMENTS = {
               // SSL cert valid until
    GERMANY_PROD:'https://api.wirecard.com', // 03/12/2020
    GERMANY_TEST:'https://api-test.wirecard.com', // 03/12/2020
    SINGAPORE_PROD:'https://api.wirecard.com.sg', // 05/21/2020
    SINGAPORE_TEST:'https://test.wirecard.com.sg', // 09/04/2020
    TORONTO_PROD:'https://engine.elastic-payments.com', // 08/27/2020
    TORONTO_TEST:'https://sandbox-engine.thesolution.com' // 12/01/2018

}

export const IOS_ENVIRONMENTS = {
    WDECEnvironmentCEEPROD: 'germanyProd',
    WDECEnvironmentCEETEST: 'germanyTest',
    WDECEnvironmentPROD: 'generalProd',
    WDECEnvironmentSingaporePROD: 'singaporeProd',
    WDECEnvironmentSingaporeTEST: 'singaporeTest',
    WDECEnvironmentTEST: 'generalTest',
    WDECEnvironmentTorontoPROD: 'torontoProd',
    WDECEnvironmentTorontoTEST: 'torontoTest',
    WDECEnvironmentTotalNumber: 'totalNumber',
    WDECEnvironmentUndefined: 'undefined',
}

export const CURRENCYS = {
    ALBANIAN_LEK:"ALL",
    ARMENIAN_DRAM:"AMD",
    ARZEBAIJAN_MANAT:"AZN",
    BELARUSIAN_RUBLE:"BYN",
    BOSNIAHERZEGOVINAMARK:"BAM",
    BRITISH_POUND:"GBP",
    BULGARIAN_LEV:"BGN",
    CROATIAN_KUNA:"HRK",
    CZECH_KORUNA:"CZK",
    DANISH_KRONE:"DKK",
    EURO:"EUR",
    GEORGIAN_LARI:"GEL",
    HUNGARIAN_FORINT:"HUF",
    ICELANDIC_KRONA:"ISK",
    MACEDONIAN_DENAR:"MKD",
    MOLDOVAN_LEU:"MDL",
    NORWEGIAN_KRONE:"NOK",
    POLISH_ZLOTY:"PLN",
    ROMANIAN_NEW_LEU:"RON",
    RUSSIAN_RUBLE:"RUB",
    SERBIAN_DINAR:"RSD",
    SWEDISH_KRONA:"SEK",
    SWISS_FRANC:"CHF",
    TURKISH_LIRA:"TRY",
    UKRAINIAN_HRYVNIA:"UAH",
    US_DOLLAR:"USD",
}


export interface IRNWirecardCcPayment {
    amount:string;
    paymentMethod:string;
    currency:string;
    requestID:string;
    merchantID:string;
    signature:string;
    token?:string;
    maskedAccountNumber?:string;
    notificationUrl?: string;
}

export default class ReactNativeWirecard{
    public static initiateClient(
        onSuccess:()=> void,
        onFailure:()=> void,
        enviroment:string){
        RNWirecard.initiateClient(enviroment,onSuccess,onFailure);
    }
    public static pay( 
        onSuccess: (error: any, events: any, transactionState: any) => void,
        onFailure: (error: any, events: any, transactionState: any) => void,
        payment:IRNWirecardCcPayment){
           RNWirecard.newPaymentRequest(
                payment,
                onSuccess,
                onFailure, 
            )
    }

}