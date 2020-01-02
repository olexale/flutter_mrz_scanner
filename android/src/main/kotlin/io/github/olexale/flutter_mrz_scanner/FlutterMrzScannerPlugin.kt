package io.github.olexale.flutter_mrz_scanner

//import co.infinum.goldeneye.GoldenEye
import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
//import io.flutter.embedding.engine.plugins.FlutterPlugin
//import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

//import top.defaults.camera.CameraView
//import top.defaults.camera.PhotographerFactory

typealias LifecycleGetter = () -> Lifecycle

class FlutterMrzScannerPlugin : FlutterPlugin, ActivityAware {
    private lateinit var lifecycle: Lifecycle

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        flutterPluginBinding.platformViewRegistry.registerViewFactory("mrzscanner", MRZScannerFactory(flutterPluginBinding.binaryMessenger, {lifecycle}))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}
    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        val actualLifecycle: Lifecycle? = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        lifecycle = actualLifecycle!!

//        binding.activity
//        lifecycle = (binding.lifecycle).lifecycle as Lifecycle
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

}


class MRZScannerFactory(private val messenger: BinaryMessenger, private val lifecycle: LifecycleGetter) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, o: Any?): PlatformView {
        return MRZScannerView(context, messenger, id, lifecycle)
    }
}

class MRZScannerView internal constructor(context: Context, messenger: BinaryMessenger, id: Int, private val lifecycle: LifecycleGetter) : PlatformView, MethodCallHandler {
    private val methodChannel: MethodChannel = MethodChannel(messenger, "mrzscanner_$id")
    private val textView: CameraXFragment = CameraXFragment(context, methodChannel, lifecycle)//, messenger)

    override fun getView(): View {
        return textView
    }

    init {
//        val goldenEye = GoldenEye.Builder(context).build()
//        textView = AntonCamera2BasicView(context)//, methodChannel)

//        webView = WebView(context)
//        textView.text = "test"
        methodChannel.setMethodCallHandler(this)
        textView.start()
//        var app = context.applicationContext as FlutterApplication
//        var photographer = PhotographerFactory.createPhotographerWithCamera2(app.currentActivity, textView)
//        photographer.startPreview()

    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        when (methodCall.method) {
//            "loadUrl" -> loadUrl(methodCall, result)
            else -> result.notImplemented()
        }
    }

    override fun dispose() {
//        textView.closeCamera()
    }
}
