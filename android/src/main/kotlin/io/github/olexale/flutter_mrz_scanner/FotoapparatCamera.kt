package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.graphics.*
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.plugin.common.MethodChannel
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import io.fotoapparat.view.CameraView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class FotoapparatCamera constructor(
        val context: Context,
        var messenger: MethodChannel) {
    private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
    private var cachedTessData: File? = null
    private var mainExecutor = ContextCompat.getMainExecutor(context)

    val cameraView = CameraView(context)
    val configuration = CameraConfiguration(frameProcessor = this::processFrame,
            pictureResolution = {
                last { it.width >= 800 }
            },
            previewResolution = {
                last { it.width >= 800 }
            })
    val fotoapparat = Fotoapparat(
            context = context,
            view = cameraView,
            cameraConfiguration = configuration
    )

    init {
        if (cachedTessData == null) {
            cachedTessData = getFileFromAssets(context, fileName = "ocrb.traineddata")
        }
    }

    fun flashlightOn() {
        fotoapparat.updateConfiguration(UpdateConfiguration(flashMode = torch()))
    }

    fun flashlightOff() {
        fotoapparat.updateConfiguration(UpdateConfiguration(flashMode = off()))
    }

    fun takePhoto(@NonNull result: MethodChannel.Result, crop: Boolean) {
        val photoResult = fotoapparat.autoFocus().takePicture()
        photoResult
                .toBitmap()
                .whenAvailable { bitmapPhoto ->
                    if (bitmapPhoto != null) {
                        val bitmap = bitmapPhoto.bitmap
                        val rotated = rotateBitmap(bitmap, rotationAngle(bitmapPhoto.rotationDegrees))
                        val stream = ByteArrayOutputStream()
                        if (crop) {
                            val cropped = calculateCutoutRect(rotated, false)
                            cropped.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val array = stream.toByteArray()
                            mainExecutor.execute {
                                result.success(array)
                            }
                        } else {
                            rotated.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val array = stream.toByteArray()
                            mainExecutor.execute {
                                result.success(array)
                            }
                        }
                    }
                }
    }

    private fun rotationAngle(rotation: Int): Int {
        if (rotation == 90)
            return -90
        if (rotation == 270)
            return 90
        if (rotation == 180)
            return 180
        return rotation
    }

    private fun processFrame(frame: Frame) {
        val bitmap = getImage(frame)
        val cropped = calculateCutoutRect(bitmap, true)
        val mrz = scanMRZ(cropped)
        val fixedMrz = extractMRZ(mrz)
        mainExecutor.execute {
            messenger.invokeMethod("onParsed", fixedMrz)
        }
    }

    private fun getImage(frame: Frame): Bitmap {
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(frame.image, ImageFormat.NV21, frame.size.width, frame.size.height, null)
        yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 100, out)
        val imageBytes = out.toByteArray()
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return rotateBitmap(image, -frame.rotation)
    }

    private fun rotateBitmap(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun scanMRZ(bitmap: Bitmap): String {
        val baseApi = TessBaseAPI()
        baseApi.init(context.cacheDir.absolutePath, "ocrb")
        baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE
        baseApi.setImage(bitmap)
        val mrz = baseApi.utF8Text
        baseApi.end()
        return mrz
    }

    private fun extractMRZ(input: String): String {
        val lines = input.split("\n")
        val mrzLength = lines.last().length
        val mrzLines = lines.takeLastWhile { it.length == mrzLength }
        val mrz = mrzLines.joinToString("\n")
        return mrz
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

    private fun calculateCutoutRect(bitmap: Bitmap, cropToMRZ: Boolean): Bitmap {
        val documentFrameRatio = 1.42 // Passport's size (ISO/IEC 7810 ID-3) is 125mm × 88mm
        val width: Double
        val height: Double

        if (bitmap.height > bitmap.width) {
            width = bitmap.width * 0.9 // Fill 90% of the width
            height = width / documentFrameRatio
        }
        else {
            height = bitmap.height * 0.75 // Fill 75% of the height
            width = height * documentFrameRatio
        }

        val mrzZoneOffset = if (cropToMRZ)  height*0.6 else 0.toDouble()
        val topOffset = (bitmap.height - height) / 2 + mrzZoneOffset
        val leftOffset = (bitmap.width - width) / 2

        return Bitmap.createBitmap(bitmap, leftOffset.toInt(), topOffset.toInt(), width.toInt(), (height - mrzZoneOffset).toInt())
    }
}