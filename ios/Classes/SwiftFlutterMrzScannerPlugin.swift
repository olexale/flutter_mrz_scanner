import Flutter
import UIKit
import AVFoundation

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
    
    var photoResult: FlutterResult?
    
    init(_ frame: CGRect, viewId: Int64, channel: FlutterMethodChannel, args: Any?) {
        self.frame = frame
        self.viewId = viewId
        self.channel = channel
        
        self.mrzview = MRZScannerView(frame: frame)
        
        super.init()
        self.mrzview.delegate = self
        
        channel.setMethodCallHandler({
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
            if (call.method == "start") {
                guard let args = call.arguments else {
                  return
                }
                if let myArgs = args as? [String: Any],
                   let isFrontCam = myArgs["isFrontCam"] as? Bool {
                    self.mrzview.startScanning(isFrontCam)
                }
            } else if (call.method == "stop") {
                self.mrzview.stopScanning()
            } else if (call.method == "flashlightOn") {
                self.toggleFlash(on: true)
            } else if (call.method == "flashlightOff") {
                self.toggleFlash(on: false)
            } else if (call.method == "takePhoto") {
                self.photoResult = result
                guard let args = call.arguments else {
                  return
                }
                if let myArgs = args as? [String: Any],
                   let shouldCrop = myArgs["crop"] as? Bool {
                    self.mrzview.takePhoto(shouldCrop: shouldCrop)
                }
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
    
    public func onPhoto(_ data: Data?) {
        guard let photoResult = photoResult else {return }
        photoResult(data)
    }
    
    private func toggleFlash(on: Bool) {
        guard let device = AVCaptureDevice.default(for: AVMediaType.video) else { return }
        guard device.hasTorch else { return }
        
        do {
            try device.lockForConfiguration()
            if (on && device.torchMode == AVCaptureDevice.TorchMode.off) {
                do {
                    try device.setTorchModeOn(level: 1.0)
                } catch {
                    print(error)
                }
            } else if (!on && device.torchMode == AVCaptureDevice.TorchMode.on) {
                device.torchMode = AVCaptureDevice.TorchMode.off
            }
            
            device.unlockForConfiguration()
        } catch {
            print(error)
        }
    }
    
}
