#import "FlutterMrzScannerPlugin.h"
#if __has_include(<flutter_mrz_scanner/flutter_mrz_scanner-Swift.h>)
#import <flutter_mrz_scanner/flutter_mrz_scanner-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_mrz_scanner-Swift.h"
#endif

@implementation FlutterMrzScannerPlugin
//+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
//  [SwiftFlutterMrzScannerPlugin registerWithRegistrar:registrar];
//}

NSObject<FlutterPluginRegistrar> *_registrar;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  self.registrar = registrar;
  FlutterMRZScannerFactory* factory = [[FlutterMRZScannerFactory alloc] initWithController:registrar.messenger];
//  factory.controller = registrar.messenger;
//  FlutterMRZScannerFactory* factory = [[FlutterMRZScannerFactory alloc] init:registrar.messenger];
  [registrar registerViewFactory:factory withId:@"mrzscanner"];
}

+ (NSObject<FlutterPluginRegistrar> *)registrar {
    return _registrar;
}

+ (void)setRegistrar:(NSObject<FlutterPluginRegistrar> *)newRegistrar {
    if (newRegistrar != _registrar) {
        _registrar = newRegistrar;
    }
}

@end
