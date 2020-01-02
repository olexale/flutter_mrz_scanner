package io.github.olexale.flutter_mrz_scanner

//import sun.tools.jconsole.inspector.Utils.getParameters

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class CameraView34(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {
    private var camera: Camera? = null
//    private val baseApi = TessBaseAPI()
    private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
    private var cachedTessData: File? = null

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        camera = Camera.open()
        camera?.setPreviewCallback(fun(data: ByteArray, camera: Camera) {
            if (data == null)
                return
            Thread(Runnable {
                val parameters = camera.parameters
                val width = parameters.previewSize.width
                val height = parameters.previewSize.height

                val yuv = YuvImage(data, parameters.previewFormat, width, height, null)

                val out = ByteArrayOutputStream()
                yuv.compressToJpeg(Rect(0, 0, width, height), 50, out)

                val bytes = out.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//            if (bitmap != null) {
//                baseApi.setImage(bitmap)
//            }

            val baseApi = TessBaseAPI()
                baseApi.init(context!!.cacheDir.absolutePath, "ocrb")
            baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE
//
            val t = Thread(Runnable {
                baseApi.setImage(bitmap)
//                recognizedText[0] = baseApi.utF8Text
                print(baseApi.utF8Text)
                baseApi.end()
            })
            t.start()
            try {
                t.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            }).start()

//            val parameters = camera.parameters
//            val width = parameters.previewSize.width
//            val height = parameters.previewSize.height
//
//            val yuv = YuvImage(data, parameters.previewFormat, width, height, null)
//
//            val out = ByteArrayOutputStream()
//            yuv.compressToJpeg(Rect(0, 0, width, height), 50, out)
//
//            val bytes = out.toByteArray()
//            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

//            val recognizedText = arrayOfNulls<String>(1)
//            val t = Thread(Runnable {
//                baseApi.setImage(bitmap)
//                recognizedText[0] = baseApi.utF8Text
//                baseApi.end()
//            })
//            t.start()
//            try {
//                t.join()
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//            print(recognizedText[0])

//            this@MyActivity.runOnUiThread(Runnable { (findViewById<View>(R.id.loopback) as ImageView).setImageBitmap(bitmap) })

////            val baos: ByteArrayOutputStream = data.compressToJpeg imageSizeRectangle, 100, baos)
//
////            val imageData: ByteArray = baos.toByteArray()

//            val previewBitmap: Bitmap? = BitmapFactory.decodeByteArray(data, 0, data.size)
//
////            result.success(recognizedText[0])
        })
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
//        setCameraDisplayOrientation(context as Activity, Camera.CameraInfo.CAMERA_FACING_BACK, camera)
        val parameters: Camera.Parameters? = camera?.parameters
        var bestSize: Camera.Size? = null

        if (parameters == null)
            return

        for (size in parameters.supportedPictureSizes) {
            if (size.width <= i1 && size.height <= i2) {
                if (bestSize == null) {
                    bestSize = size
                } else {
                    val bestArea: Int = bestSize.width * bestSize.height
                    val newArea: Int = size.width * size.height
                    if (newArea > bestArea) {
                        bestSize = size
                    }
                }
            }
        }
        if (bestSize == null)
            return

        parameters.setPictureSize(bestSize.width, bestSize.height)
//        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
//        camera?.parameters = parameters
        try {
            camera?.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        camera?.startPreview()

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    companion object {
        fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: Camera?) {
            val info = CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            val rotation = activity.windowManager.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
            var result: Int
            if (info.facing === Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else {
                result = (info.orientation - degrees + 360) % 360
            }
            camera?.setDisplayOrientation(result)
        }
    }

    init {
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        if (cachedTessData == null) {
            cachedTessData = getFileFromAssets(context!!, fileName = "ocrb.traineddata")
        }
//        baseApi.init(context!!.cacheDir.absolutePath, "ocrb")
//        baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE
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
