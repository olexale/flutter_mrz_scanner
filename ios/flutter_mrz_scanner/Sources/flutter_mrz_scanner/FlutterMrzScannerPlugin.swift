import Flutter

public class FlutterMrzScannerPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let factory = FlutterMRZScannerFactory(controller: registrar.messenger())
        registrar.register(factory, withId: "mrzscanner")
    }
}
