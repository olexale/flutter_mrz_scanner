import Flutter
import UIKit

import SwiftyTesseract

public class SwiftFlutterMrzScannerPlugin: NSObject, FlutterPlugin {
    fileprivate var tesseract: SwiftyTesseract!

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_mrz_scanner", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterMrzScannerPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if (call.method  == "recognize") {
            if (tesseract == nil) {
                initTesseract()
            }
            guard let params = call.arguments as? [String : Any],
                let raw = params["image"] as? String,
                let data = Data.init(base64Encoded: raw, options: .init(rawValue: 0)),
                let image = UIImage.init(data: data) else {
                    result("error")
                    return
            }
            
            var recognizedString: String?
            tesseract.performOCR(on: image) { recognizedString = $0 }
            result(recognizedString)
        }
    }
    
    func initTesseract() {
        tesseract = SwiftyTesseract(language: .custom("ocrb"), bundle: Bundle(url: Bundle(for: SwiftFlutterMrzScannerPlugin.self).url(forResource: "TraineedDataBundle", withExtension: "bundle")!)!, engineMode: .tesseractLstmCombined)
    }
}
