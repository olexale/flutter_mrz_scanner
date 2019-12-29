import Flutter
import UIKit

import SwiftyTesseract

@objc public class FlutterMRZScannerFactory: NSObject, FlutterPlatformViewFactory {
  
  let controller: FlutterBinaryMessenger
  
  @objc public init(controller: FlutterBinaryMessenger) {
    self.controller = controller
  }
  
  @objc public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
    let channel = FlutterMethodChannel(
      name: "mrzscanner_" + String(viewId),
      binaryMessenger: controller
    )
    return FlutterMRZScanner(frame, viewId: viewId, channel: channel, args: args)
  }
  
  
}

public class FlutterMRZScanner: NSObject, FlutterPlatformView, MRZScannerViewDelegate {
  
  
  let frame: CGRect
  let viewId: Int64
  let channel: FlutterMethodChannel
  let mrzview: UIView
  
  init(_ frame: CGRect, viewId: Int64, channel: FlutterMethodChannel, args: Any?) {
    self.frame = frame
    self.viewId = viewId
    self.channel = channel
    
    self.mrzview = MRZScannerView(frame: frame)
    
    super.init()
    
    //      channel.setMethodCallHandler({
    //          (call: FlutterMethodCall, result: FlutterResult) -> Void in
    //          if (call.method == "loadUrl") {
    //              let url = call.arguments as! String
    //              webview.load(URLRequest(url: URL(string: url)!))
    //          }
    //      })
  }
  
  public func view() -> UIView {
    return self.mrzview
  }
  
  public func onParse(_ parsed: String?) {
    self.channel.invokeMethod("onParse", arguments: parsed)
  }
  
  public func onError(_ error: String?) {
    self.channel.invokeMethod("onError", arguments: error)
  }
  
}

//public class SwiftFlutterMrzScannerPlugin: NSObject, FlutterPlugin {
//  fileprivate var tesseract: SwiftyTesseract!
//
//  public static func register(with registrar: FlutterPluginRegistrar) {
//    let channel = FlutterMethodChannel(name: "flutter_mrz_scanner", binaryMessenger: registrar.messenger())
//    let instance = SwiftFlutterMrzScannerPlugin()
//    registrar.addMethodCallDelegate(instance, channel: channel)
//  }
//
//  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
//    if (call.method  == "recognize") {
//      if (tesseract == nil) {
//        initTesseract()
//      }
//      guard let params = call.arguments as? [String : Any],
//        let raw = params["image"] as? String,
//        let data = Data.init(base64Encoded: raw, options: .init(rawValue: 0)),
//        let image = UIImage.init(data: data) else {
//          result("error")
//          return
//      }
//
//      var recognizedString: String?
//      tesseract.performOCR(on: image) { recognizedString = $0 }
//      result(recognizedString)
//    }
//  }
//
//  func initTesseract() {
//    tesseract = SwiftyTesseract(language: .custom("ocrb"), bundle: Bundle(url: Bundle(for: SwiftFlutterMrzScannerPlugin.self).url(forResource: "TraineedDataBundle", withExtension: "bundle")!)!, engineMode: .tesseractLstmCombined)
//  }
//}
