package io.github.olexale.flutter_mrz_scanner

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
//import sun.tools.jconsole.inspector.Utils.getParameters
import java.io.IOException


class CameraView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {
    private var camera: Camera? = null
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        camera = Camera.open()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        setCameraDisplayOrientation(context as Activity, Camera.CameraInfo.CAMERA_FACING_BACK, camera)
        val parameters: Camera.Parameters? = camera?.getParameters()
        var bestSize: Camera.Size? = null

        if (parameters == null)
            return

        for (size in parameters.getSupportedPictureSizes()) {
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
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        camera?.setParameters(parameters)
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
    }
}