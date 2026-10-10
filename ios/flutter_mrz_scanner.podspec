#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_mrz_scanner.podspec' to validate before publishing.
#
# This plugin also ships a Swift package at ios/flutter_mrz_scanner/Package.swift.
# Apps built with Swift Package Manager use that one and never see this podspec.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_mrz_scanner'
  s.version          = '2.3.0'
  s.summary          = 'Scans MRZ (Machine Readable Zone) from identity documents.'
  s.description      = <<-DESC
Scans MRZ (Machine Readable Zone) from identity documents for iOS and Android.
                       DESC
  s.homepage         = 'https://github.com/olexale/flutter_mrz_scanner'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Oleksandr Leushchenko' => 'olexa.le@gmail.com' }
  s.source           = { :path => '.' }

  # Only the plugin's own sources. Vendor/ holds the SwiftyTesseract 4.x sources
  # used by Swift Package Manager builds; CocoaPods builds get the 3.1.3 pod
  # instead, so those files must stay out of this target.
  s.source_files = 'flutter_mrz_scanner/Sources/flutter_mrz_scanner/*.swift'
  s.dependency 'Flutter'
  s.dependency 'SwiftyTesseract'
  s.platform = :ios, '13.0'
  s.resource_bundles = { 'TraineedDataBundle' => ['flutter_mrz_scanner/Sources/flutter_mrz_scanner/Resources/tessdata'] }

  # SwiftyTesseract 3.1.3 ships no arm64 simulator slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  s.swift_version = '5.2'
end
