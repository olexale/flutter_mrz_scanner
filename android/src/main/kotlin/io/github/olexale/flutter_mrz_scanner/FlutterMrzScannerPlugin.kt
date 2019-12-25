package io.github.olexale.flutter_mrz_scanner

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import io.flutter.util.PathUtils.getFilesDir




/** FlutterMrzScannerPlugin */
public class FlutterMrzScannerPlugin: FlutterPlugin, MethodCallHandler {
  private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
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
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_mrz_scanner")
      channel.setMethodCallHandler(FlutterMrzScannerPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
      if (call.method == "recognize") {
//          val datapath = getFilesDir() + "/tesseract/"
//          mTess = TessBaseAPI()
//
//          checkFile(File(datapath + "tessdata/"))

          val tessDataPath = call.argument<String>("tessData")
          val recognizedText = arrayOfNulls<String>(1)
          val baseApi = TessBaseAPI()
          baseApi.init(tessDataPath, "ocrb")
//        val tempFile = File(imagePath)
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
}
