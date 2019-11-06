
# react-native-wirecard
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION
ITS NOT READY FOR PRODUCTION

this package is currently in development v2.0.0 will be usable .
dont waste your time implementing any version below.
## Getting started

`$ npm install react-native-wirecard --save`

### Manual installation


#### iOS

1. Add this to you podfile
`pod 'wirecard-react-native', :path=>'../node_modules/wirecard-react-native/ios'`

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.rnwirecard.RNWirecardPackage;` to the imports at the top of the file
  - Add `new RNWirecardPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':wirecard-react-native'
  	project(':wirecard-react-native').projectDir = new File(rootProject.projectDir, 	'../node_modules/wirecard-react-native/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':wirecard-react-native')
  	```


## Usage
```javascript
import ReactNativeWirecard,{
	IRNWirecardCcPayment,
  PAYMENT_METHODS,
	IOS_ENVIRONMENTS,
	ANDROID_ENVIRONMENTS,CURRENCYS,
	TRANSACTION_TYPES} from 'wirecard-react-native';

export default class PaymentManager {
	constructor(){
		const environment = Platform.OS === 'ios' 
		? 
		IOS_ENVIRONMENTS.GERMANY_TEST 
		:
		ANDROID_ENVIRONMENTS.GERMANY_TEST;

		ReactNativeWirecard.initiateClient(environment,this.onInitSuccess.bind(this),this.onInitFail.bind(this));
	}

	onInitSuccess(){
		console.log('client was initiated successfully');
	}
	onInitFail(){
		console.log('client failed to initiate');
	}
  makePayment(){
		const mockPayment: IRNWirecardCcPayment = {
			amount: '1.99',
			currency: CURRENCYS.EURO,
			transactionType:TRANSACTION_TYPES.PURCHASE, // only purchase supported for now
			paymentMethod: PAYMENT_METHODS.CARD.paymentMethod,
			signature:'baCkEndGeneRat3D5iGN4TuR3.==',
			requestID:'my-internal-id',
			merchantID: 'wirecard-assigned-id',
			notificationUrl:'https://my-backend.com/api/my-notifications-endpoint',
		}
		// you can add a token to IRNWirecardCcPayment if you have it token: '2y3423423saa2';
		// this should trigger the Wirecard UI;
		ReactNativeWirecard.pay(
			this.onPaymentSuccess.bind(this),
			this.onPaymentFailure.bind(this),
			mockPayment);
	}

  onPaymentSuccess(
		error:any,
		tokenId:any,
		transactionState:any,
		transactionId: any,
		requestId: any
		){
			console.log(`transaction${transactionId} done succesfully!`);
	}
	onPaymentFailed(
		error:any,
		tokenId:any,
		transactionState:any,
		transactionId: any,
		requestId: any
	){
		console.log(`transaction failed :( ${error}`));
	}
}

// TODO: What to do with the module?
RNWirecard;
```
  