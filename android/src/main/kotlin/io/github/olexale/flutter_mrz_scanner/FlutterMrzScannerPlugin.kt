package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.fotoapparat.characteristic.LensPosition
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.Configuration
import io.fotoapparat.selector.LensPositionSelector
import io.fotoapparat.selector.front

class FlutterMrzScannerPlugin : FlutterPlugin {

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding.platformViewRegistry.registerViewFactory("mrzscanner", MRZScannerFactory(flutterPluginBinding.binaryMessenger))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}
}

class MRZScannerFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, o: Any?): PlatformView {
        return MRZScannerView(context, messenger, id)
    }
}

class MRZScannerView internal constructor(context: Context, messenger: BinaryMessenger, id: Int) : PlatformView, MethodChannel.MethodCallHandler {
    private val methodChannel: MethodChannel = MethodChannel(messenger, "mrzscanner_$id")
    private val cameraView: FotoapparatCamera = FotoapparatCamera(context, methodChannel)//, messenger)

    override fun getView(): View = cameraView.cameraView

    init {
        methodChannel.setMethodCallHandler(this)
//        cameraView.fotoapparat.start()
    }

    override fun dispose() {
        cameraView.fotoapparat.stop()
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "start" -> {
                val isFrontCam = call.argument<Boolean>("isFrontCam")
                if (isFrontCam!!) {
                    cameraView.fotoapparat.switchTo(front(), cameraView.configuration)
                }
                cameraView.fotoapparat.start()
                result.success(null)
            }
            "stop" -> {
                cameraView.fotoapparat.stop()
                result.success(null)
            }
            "flashlightOn" -> {
                cameraView.flashlightOn()
                result.success(null)
            }
            "flashlightOff" -> {
                cameraView.flashlightOff()
                result.success(null)
            }
            "takePhoto" -> {
                val shouldCrop = call.argument<Boolean>("crop")
                shouldCrop?.let { cameraView.takePhoto(result, crop = it) }
            }
            else -> {
                result.notImplemented()
            }
        }
    }
}