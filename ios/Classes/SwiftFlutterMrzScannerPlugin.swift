import Flutter
import UIKit

import SwiftyTesseract

public class SwiftFlutterMrzScannerPlugin: NSObject, FlutterPlugin {
//  fileprivate var tesseract = SwiftyTesseract(language: .custom("ocrb"), bundle: Bundle(url: Bundle(for: SwiftFlutterMrzScannerPlugin.self).url(forResource: "TraineedDataBundle", withExtension: "bundle")!)!, engineMode: .tesseractLstmCombined)
  fileprivate var tesseract = SwiftyTesseract(language: .custom("ocrb"),
//                                              bundle: Bundle(for: GeneratedPluginRegistrant.self),
                                              engineMode: .tesseractLstmCombined)

//  fileprivate var tesseract = SwiftyTesseract(language: .custom("ocrb"), bundle: Bundle.init(identifier: "TraineedDataBundle")!, engineMode: .tesseractLstmCombined)
  
  public static func register(with registrar: FlutterPluginRegistrar) {
//    let bundle = Bundle(url: Bundle(for: SwiftFlutterMrzScannerPlugin.self).url(forResource: "TraineedDataBundle", withExtension: "bundle")!)!
    
//    let file = bundle.url(forResource: "ocrb", withExtension: "traineddata")
//    let qq = bundle.
    
        
    let channel = FlutterMethodChannel(name: "flutter_mrz_scanner", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterMrzScannerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if (call.method  == "recognize") {
      guard let params = call.arguments as? [String : Any],
            let raw = params["image"] as? String,
            let data = Data.init(base64Encoded: raw, options: .init(rawValue: 0)),
//            let data = raw.data(using: .utf8),
            let image = UIImage.init(data: data) else {
          result("error")
          return
      }
      
      var recognizedString: String?
      tesseract.performOCR(on: image) { recognizedString = $0 }
      result(recognizedString)
    }
  }
}
