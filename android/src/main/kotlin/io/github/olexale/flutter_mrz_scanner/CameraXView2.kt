package io.github.olexale.flutter_mrz_scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log


class CameraXView2 @JvmOverloads constructor(
        context: Context,
        var messenger: MethodChannel,
        private val lifecycle: LifecycleGetter,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : TextureView(context, attrs, defStyle), LifecycleOwner {

    private val executor = Executors.newSingleThreadExecutor()

    override fun getLifecycle(): Lifecycle = lifecycle()

    fun startCamera() {
        // Get screen metrics used to setup camera for full screen resolution
//        val metrics = DisplayMetrics().also {
//            this.display.getRealMetrics(it)
//        }
//        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
//        print(width)
//        print(height)
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
//            setTargetResolution(Size(width, height))
            setTargetResolution(Size(1200, 1920))
//            setLensFacing(CameraX.LensFacing.BACK)
            // We request aspect ratio but no resolution to let CameraX optimize our use cases
//            setTargetAspectRatio(screenAspectRatio)
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            this.surfaceTexture = it.surfaceTexture
            updateTransform()
            adjustAspectRatio(1200, 1920)
        }
//        val rso = previewConfig.
//        val with = bitmap.width

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                    ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val textureView = this
        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, MRZAnalyzer(context, textureView, messenger))
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }

    fun updateTransform() {
        if (this.display == null)
            return
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = this.width / 2f
        val centerY = this.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(this.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        this.setTransform(matrix)
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val viewWidth = this.getWidth()
        val viewHeight = this.getHeight()
        val aspectRatio = videoHeight.toDouble() / videoWidth

        val newWidth: Int
        val newHeight: Int
        if (viewHeight < (viewWidth * aspectRatio).toInt()) {
            // limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            // limited by short height; restrict width
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }
        val xoff = (viewWidth - newWidth) / 2
        val yoff = (viewHeight - newHeight) / 2

        val txform = Matrix()
        this.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        txform.postTranslate(xoff.toFloat(), yoff.toFloat())
        this.setTransform(txform)
    }

    private fun aspectRatio(width: Int, height: Int): AspectRatio {
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - CameraXView2.RATIO_4_3_VALUE) <= abs(previewRatio - CameraXView2.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}

private class MRZAnalyzer (val context: Context, val textureView: TextureView, var messenger: MethodChannel) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L

    private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
    private var cachedTessData: File? = null

    private var mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    init {
        if (cachedTessData == null) {
            cachedTessData = getFileFromAssets(context, fileName = "ocrb.traineddata")
        }
    }

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        // Calculate the average luma no more often than every second
        if (currentTimestamp - lastAnalyzedTimestamp >=
                TimeUnit.MILLISECONDS.toMillis(400)) {
            val bitmap = textureView.bitmap
            if (bitmap != null) {
                val scaled = scaleImage(bitmap)
                val cropped = getMRZBitmap(scaled)
                val mrz = scanMRZ(cropped)
                val fixedMrz = extractMRZ(mrz)
                if (fixedMrz != "") {
                    Log.v("CameraXView2", fixedMrz)
                    mainExecutor.execute {
                        messenger.invokeMethod("onParsed", fixedMrz)
                    }
                }
            }
            // Update timestamp of last analyzed frame
            lastAnalyzedTimestamp = currentTimestamp
        }
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
        if (lines.count() < 2) {
            return ""
        }
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

    private fun scaleImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        textureView.getTransform(matrix)
        matrix.setScale((textureView.width/1200).toFloat(), (textureView.height/1920).toFloat())
        return Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height, matrix,true)
    }

    private fun getMRZBitmap(bitmap: Bitmap): Bitmap {
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

        val mrzZoneOffset = height*0.6
        val topOffset = (bitmap.height - height) / 2 + mrzZoneOffset
        val leftOffset = (bitmap.width - width) / 2

        return Bitmap.createBitmap(bitmap, leftOffset.toInt(), topOffset.toInt(), width.toInt(), (height - mrzZoneOffset).toInt())
    }
}