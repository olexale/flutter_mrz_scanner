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
    let mrzview: MRZScannerView
    
    init(_ frame: CGRect, viewId: Int64, channel: FlutterMethodChannel, args: Any?) {
        self.frame = frame
        self.viewId = viewId
        self.channel = channel
        
        self.mrzview = MRZScannerView(frame: frame)
        
        super.init()
        self.mrzview.delegate = self
        
        channel.setMethodCallHandler({
            (call: FlutterMethodCall, result: FlutterResult) -> Void in
            if (call.method == "start") {
                self.mrzview.startScanning()
            } else if (call.method == "stop") {
                self.mrzview.stopScanning()
            }
        })
    }
    
    public func view() -> UIView {
        return self.mrzview
    }
    
    public func onParse(_ parsed: String?) {
        self.channel.invokeMethod("onParsed", arguments: parsed)
    }
    
    public func onError(_ error: String?) {
        self.channel.invokeMethod("onError", arguments: error)
    }
    
}
