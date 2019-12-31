package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import java.io.File
import java.io.IOException

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

class MRZScannerView internal constructor(context: Context, messenger: BinaryMessenger, id: Int) : PlatformView, MethodCallHandler {
    private val textView: CameraView = CameraView(context)
    private val methodChannel: MethodChannel = MethodChannel(messenger, "mrzscanner_$id")

    override fun getView(): View {
        return textView
    }

    init {
//        webView = WebView(context)
//        textView.text = "test"
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
//            "loadUrl" -> loadUrl(methodCall, result)
            else -> result.notImplemented()
        }
    }

    override fun dispose() {
        // TODO dispose actions if needed
    }
}
