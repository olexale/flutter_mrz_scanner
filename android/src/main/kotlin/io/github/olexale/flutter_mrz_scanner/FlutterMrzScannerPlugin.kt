package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.NonNull
import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
import java.io.IOException


/** FlutterMrzScannerPlugin */
public class FlutterMrzScannerPlugin : FlutterPlugin, MethodCallHandler {
    private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        appContext = appContext ?: flutterPluginBinding.applicationContext

        val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_mrz_scanner")
        channel.setMethodCallHandler(FlutterMrzScannerPlugin());
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {

        private var appContext: Context? = null
        private var cachedTessData: File? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_mrz_scanner")
            channel.setMethodCallHandler(FlutterMrzScannerPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "recognize") {
            val recognizedText = arrayOfNulls<String>(1)
            val baseApi = TessBaseAPI()

            if (cachedTessData == null) {
                cachedTessData = getFileFromAssets(context = appContext!!, fileName = "ocrb.traineddata")
            }
            baseApi.init(appContext!!.cacheDir.absolutePath, "ocrb")
            baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE

            val t = Thread(Runnable {
                val base64Image = call.argument<String>("image")
                val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                baseApi.setImage(decodedByte)
                recognizedText[0] = baseApi.utF8Text
                baseApi.end()
            })
            t.start()
            try {
                t.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            result.success(recognizedText[0])

        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File {
        val directory = File(context.cacheDir, "tessdata/")
        directory.mkdir()
        return File(directory, fileName)
                .also { file ->
                    file.outputStream().use { cache ->
                        context.assets.open(fileName).use { stream ->
                            stream.copyTo(cache)
                        }
                    }
                }
    }
}
