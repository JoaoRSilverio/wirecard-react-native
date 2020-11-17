
# wirecard-react-native
A React-Native wrapper for the Wirecard SDK's
Android: com.wirecard.ecom:card 3.4.0
Ios: paymentSDK/All 3.6.0

this package is currently in development v2.0.0 will be production ready .

## react-native versions supported:
0.60.0
0.61.0
0.62.2
## payment methods supported
- Credit Card
- Google Pay
## Getting started
- follow react-native instructions to setup a new project if you dont have one http://reactnative.dev/docs/environment-setup
- `$ npm install wirecard-react-native --save`
- time saving note
	`if your fresh react-native install fails when first running, try this:
	- run npm install
	- android / build.gradle -> add
		configurations.all{
		    resolutionStrategy {
        		force "com.facebook.soloader:soloader:0.8.2"
    		}
		}

	- android / app / build.gradle -> add
			packagingOptions {
    			pickFirst 'lib/x86_64/libjsc.so'
    			pickFirst 'lib/arm64-v8a/libjsc.so'
  			}
	- run react-native start 
	- run react-native run-android
	`

#### iOS

1. Add this to you podfile
`pod 'wirecard-react-native', :path=>'../node_modules/wirecard-react-native/ios'`

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.rnwirecard.RNWirecardPackage;` to the imports at the top of the file
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':wirecard-react-native'
  	project(':wirecard-react-native').projectDir = new File(rootProject.projectDir, 	'../node_modules/wirecard-react-native/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':wirecard-react-native')
  	```
4. also change 
	```
	defaultConfig {
		minSdk 19
		multiDexEnabled true
	}
	```
5. Insert in `android/build.gradle`
	allprojects{
		repositories {
			maven {
            		  url  "https://dl.bintray.com/wirecard/ecom-android"
        	}
		}
	}
## Usage
```typescript
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

		ReactNativeWirecard.initiateClient(this.onInitSuccess.bind(this),this.onInitFail.bind(this), environment);
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
			// transactionType:TRANSACTION_TYPES.PURCHASE, // only purchase supported for now
			paymentMethod: PAYMENT_METHODS.CARD.paymentMethod,
			signature:'baCkEndGeneRat3D5iGN4TuR3.==',
			requestID:'my-internal-id',
			merchantID: 'wirecard-assigned-id',
			notificationUrl:'https://my-backend.com/api/my-notifications-endpoint',
			setAttempt3d: false,
    		setRecurring: false,
    		setRequireManualCardBrandSelection: false,
   			setAnimatedCardPayment: true
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
		console.log(`transaction failed :( ${error}`);
	}
}

```
### IRNWirecardCcPayment

amount: string;
paymentMethod: string;
currency: string;
requestID: string;
merchantID: string;
signature: string;
token?: string;
maskedAccountNumber?: string;
notificationUrl?: string;

### IRNWirecardCCPaymentResponse
error: string; // is empty on success
tokenId: string;
transactionState: string;
transactionId: string;
requestId: string;

```
  