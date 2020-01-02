package io.github.olexale.flutter_mrz_scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class AntonCamera2BasicView @JvmOverloads constructor(
        context: Context,
        var messenger: MethodChannel,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : AutoFitTextureView(context, attrs, defStyle) {

    private var antonTextureListener = object : SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
//            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true
        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    private lateinit var cameraId: String
//    private lateinit var preview2: ImageView
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size

    private val DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
    private var cachedTessData: File? = null
    private val baseApi: TessBaseAPI = TessBaseAPI()

    init {
        if (cachedTessData == null) {
            cachedTessData = getFileFromAssets(context, fileName = "ocrb.traineddata")
        }
//        val baseApi = TessBaseAPI()
        baseApi.init(context.cacheDir.absolutePath, "ocrb")
        baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE
    }

    fun start() {
        if (isAvailable) {
            openCamera(width, height)
        } else {
            this.surfaceTextureListener = antonTextureListener
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@AntonCamera2BasicView.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@AntonCamera2BasicView.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
        }

    }

    private var backgroundHandler: Handler? = null
    private var imageReader: ImageReader? = null

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { imageReader ->
        if (!isProcessing) {
            isProcessing = true
            val image: Image?
            try {
                image = imageReader.acquireLatestImage()
                if (image != null) {
                    val buffer = image.planes[0].buffer
                    val bitmap = fromByteBuffer(buffer)
                    image.close()
                val cropped = calculateCutoutRect(bitmap)
                val mrz = scanMRZ(cropped)
                    messenger.invokeMethod("onParsed", mrz)
                }
            } catch (e: Exception) {
                //
                Log.e(TAG, e.toString())
            }
            finally {
                isProcessing = false
            }
        }
    }

    private var isProcessing = false

    private fun scanMRZ(bitmap: Bitmap) : String? {
        var text: String? = null
        val t = Thread(Runnable {
//        val baseApi = TessBaseAPI()
//        baseApi.init(context.cacheDir.absolutePath, "ocrb")
//        baseApi.pageSegMode = DEFAULT_PAGE_SEG_MODE
//            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            baseApi.setImage(bitmap)
            text = baseApi.utF8Text
//            recognizedText[0] = baseApi.utF8Text
//            Log.e(TAG, baseApi.utF8Text)
//            print(baseApi.utF8Text)
//            baseApi.end()


//            messenger.invokeMethod("onParsed", baseApi.utF8Text)
        })
        t.start()
        try {
            t.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return text
    }

    private fun fromByteBuffer(buffer: ByteBuffer): Bitmap {
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes, 0, bytes.size)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//        return invert(decodeByteArray)
    }

    private fun calculateCutoutRect(bitmap: Bitmap): Bitmap {
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

        val topOffset = (bitmap.height - height) / 2
        val leftOffset = (bitmap.width - width) / 2

        return Bitmap.createBitmap(bitmap, leftOffset.toInt(), topOffset.toInt(), width.toInt(), height.toInt())
    }

    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest

    private val cameraOpenCloseLock = Semaphore(1)
    private var flashSupported = false

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                        cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    continue
                }

                val map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // For still image captures, we use the largest available size.
                val allSizes = listOf(*map.getOutputSizes(ImageFormat.JPEG))
                val usedSize = allSizes[20]
//                val largest = Collections.max(listOf(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
                val largest = allSizes[15]
                imageReader = ImageReader.newInstance(
                        usedSize.width, usedSize.height,
                        ImageFormat.JPEG, /*maxImages*/ 2
                ).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                previewSize = ImageHelper.chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture::class.java),
                        this.width, this.height, this.width, this.height,
//                        rotatedPreviewWidth, rotatedPreviewHeight,
//                        maxPreviewWidth, maxPreviewHeight,
                        largest
                )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    this.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    this.setAspectRatio(previewSize.height, previewSize.width)
                }

                // Check if the flash is supported.
                flashSupported =
                        characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                // We've found a viable camera and finished setting up member variables,
                // so we don't need to iterate through other available cameras.
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
//            ErrorDialog.newInstance(getString(R.string.camera_error))
//                    .show(childFragmentManager, FRAGMENT_DIALOG)
        }

    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
//        configureTransform(width, height)
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }

    }

    fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = this.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(texture)
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)
            previewRequestBuilder.addTarget(imageReader?.surface!!)

            cameraDevice?.createCaptureSession(
                    listOf(surface, imageReader?.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            if (cameraDevice == null) return

                            captureSession = cameraCaptureSession
                            try {
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                )
                                setAutoFlash(previewRequestBuilder)
                                previewRequest = previewRequestBuilder.build()
                                captureSession?.setRepeatingRequest(previewRequest, null, null)
                            } catch (e: CameraAccessException) {
                                Log.e(TAG, e.toString())
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Toast.makeText(context, "Failed to capture camera", Toast.LENGTH_LONG)
                                    .show()
                        }
                    }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    companion object {
        private const val TAG = "Camera2BasicFragment"
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