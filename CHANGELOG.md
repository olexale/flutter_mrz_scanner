## 2.3.0

* Add Swift Package Manager support for iOS. CocoaPods keeps working, so no
  action is needed for existing projects.
* Swift Package Manager builds run on Apple Silicon simulators. The CocoaPods
  build still cannot, because SwiftyTesseract 3.1.3 ships no arm64 simulator
  slice.
* Replace the Objective-C plugin registrant with Swift — a Swift package target
  cannot mix the two languages.
* Vendor the SwiftyTesseract 4.0.1 sources (MIT) for Swift Package Manager
  builds. The upstream project was archived in 2022 and never published 4.x to
  the CocoaPods trunk. Tesseract itself now comes from a mirror of the archived
  `libtesseract` binary package.
* Raise the iOS deployment target from 12 to 13, matching Flutter's minimum.
* Require Flutter 3.44.0 or newer, the first release that provides the
  `FlutterFramework` Swift package the plugin depends on.

## 2.2.1

* Fix Android 16KB Page size incompatibility by updating tesseract4android to 4.9.0 (by @khamidjon)

## 2.2.0

* Fix repository for 'io.fotoapparat:fotoapparat:2.7.0'
* Update example

## 2.1.1

* Add namespace (by @makhosi6)

## 2.1.0

* Support for Flutter 3.0.5 (by @dadagov125)

## 2.0.1

* Fix : Android crash (by @eusopht2021)

## 2.0.0

* Fix : iOS compiling errors for iOS 15 (by @gdaguin)
* Improvements : on Android, the camera is now focusing automatically (by @gdaguin)
* Port to null safety

## 1.0.0
* Android version redeveloped with Fotoapparat library
* Add overlay widget
* Flashlight on/off
* Take a photo

## 0.7.1

* Provide possibility to disable overlay

## 0.7.0

* First public version with basic iOS and Android scanners
