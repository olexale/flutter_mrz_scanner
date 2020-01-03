package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

typealias LifecycleGetter = () -> Lifecycle

class FlutterMrzScannerPlugin : FlutterPlugin, ActivityAware {
    private lateinit var lifecycle: Lifecycle

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding.platformViewRegistry.registerViewFactory("mrzscanner", MRZScannerFactory(flutterPluginBinding.binaryMessenger, { lifecycle }))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}
    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        val actualLifecycle: Lifecycle? = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        lifecycle = actualLifecycle!!
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}

class MRZScannerFactory(private val messenger: BinaryMessenger, private val lifecycle: LifecycleGetter) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, o: Any?): PlatformView {
        return MRZScannerView(context, messenger, id, lifecycle)
    }
}

class MRZScannerView internal constructor(context: Context, messenger: BinaryMessenger, id: Int, lifecycle: LifecycleGetter) : PlatformView {
    private val methodChannel: MethodChannel = MethodChannel(messenger, "mrzscanner_$id")
    private val cameraView: CameraXView2 = CameraXView2(context, methodChannel, lifecycle)//, messenger)

    override fun getView(): View = cameraView

    init {
        cameraView.post { cameraView.startCamera() }
//        cameraView.startCamera()
    }

    override fun dispose() {
//        cameraView.closeCamera()
    }
}