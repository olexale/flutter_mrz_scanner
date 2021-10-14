# flutter_mrz_scanner

Scan MRZ (Machine Readable Zone) from identity documents (passport, id, visa) using iOS and Android. Heavily insipred by [QKMRZScanner](https://github.com/Mattijah/QKMRZScanner).

## To plugin users
Hello,

Sorry that the plugin didn’t work well for all of you. Unfortunately, I don’t have testing devices and time for investigations and fixing issues at the moment. Please feel free to create new tickets. I would be even more grateful for pull requests.
I’m not abandoning the plugin and promise to merge contributions with fixes and new functionality, hence I would kindly ask for help with the development 🙂

Thanks in advance! 

### Supported formats:
* TD1
* TD2
* TD3
* MRV-A
* MRV-B

## Usage

### Import the package
Add to `pubspec.yaml`
```yaml
dependencies:
  flutter_mrz_scanner: ^2.0.0
```
### For iOS
Set iOS deployment target to 12.
The plugin uses the device camera, so do not forget to provide the `NSCameraUsageDescription`. You may specify it in `Info.plist` like that:
```xml
    <key>NSCameraUsageDescription</key>
    <string>May I scan a MRZ please?</string>
```

### For Android
Add
```
<uses-permission android:name="android.permission.CAMERA" />
```
to `AndroidManifest.xml`

### Use the widget
Use `MRZScanner` widget:
```dart
MRZScanner(
  withOverlay: true, // optional overlay
  onControllerCreated: (controller) =>
    onControllerCreated(controller),
  )
```
Refer to `example` project for the complete app sample.

## Acknowledgements
* [Anna Domashych](https://github.com/foxanna) for helping with [mrz_parser](https://github.com/olexale/mrz_parser) implementation in Dart
* [Anton Derevyanko](https://github.com/antonderevyanko) for hours of Android-related discussions
* [Mattijah](https://github.com/Mattijah) for beautiful [QKMRZScanner](https://github.com/Mattijah/QKMRZScanner) library

## License
`flutter_mrz_scanner` is released under a [MIT License](https://opensource.org/licenses/MIT). See `LICENSE` for details.
