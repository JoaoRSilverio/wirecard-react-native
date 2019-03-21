
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

### Mostly automatic installation

`$ react-native link react-native-wirecard`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-wirecard` and add `RNWirecard.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNWirecard.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNWirecardPackage;` to the imports at the top of the file
  - Add `new RNWirecardPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-wirecard'
  	project(':react-native-wirecard').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-wirecard/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-wirecard')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNWirecard.sln` in `node_modules/react-native-wirecard/windows/RNWirecard.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Wirecard.RNWirecard;` to the usings at the top of the file
  - Add `new RNWirecardPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNWirecard from 'react-native-wirecard';

// TODO: What to do with the module?
RNWirecard;
```
  