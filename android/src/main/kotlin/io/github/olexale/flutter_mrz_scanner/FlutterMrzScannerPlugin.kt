package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

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

class MRZScannerView internal constructor(context: Context, messenger: BinaryMessenger, id: Int) : PlatformView {
    private val methodChannel: MethodChannel = MethodChannel(messenger, "mrzscanner_$id")
    private val cameraView: FotoapparatCamera = FotoapparatCamera(context, methodChannel)//, messenger)

    override fun getView(): View = cameraView.cameraView

    init {
        cameraView.fotoapparat.start()
    }

    override fun dispose() {
        cameraView.fotoapparat.stop()
    }
}