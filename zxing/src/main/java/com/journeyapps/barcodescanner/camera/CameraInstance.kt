package com.journeyapps.barcodescanner.camera

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import com.journeyapps.barcodescanner.R
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.Util

/**
 * Manage a camera instance using a background thread.
 *
 * All methods must be called from the main thread.
 */
open class CameraInstance(cameraManager: CameraManager) {
    /**
     * @return the CameraThread used to manage the camera
     */
    protected var cameraThread: CameraThread = CameraThread.instance
        private set

    /**
     * @return the surface om which the preview is displayed
     */
    var surface: CameraSurface? = null

    /**
     * Returns the CameraManager used to control the camera.
     *
     *
     * The CameraManager is not thread-safe, and must only be used from the CameraThread.
     *
     * @return the CameraManager used
     */
    protected var cameraManager: CameraManager = cameraManager
        private set

    private var readyHandler: Handler? = null

    var displayConfiguration: DisplayConfiguration? = null
        set(configuration) {
            field = configuration
            cameraManager.displayConfiguration = configuration
        }

    var isOpen = false
        private set

    var isCameraClosed = true
        private set

    private var mainHandler = Handler()

    private var cameraSettings = CameraSettings()

    private val opener = Runnable {
        try {
            Log.d(TAG, "Opening camera")
            cameraManager.open()
        } catch (e: Exception) {
            notifyError(e)
            Log.e(TAG, "Failed to open camera", e)
        }
    }
    private val configure = Runnable {
        try {
            Log.d(TAG, "Configuring camera")
            cameraManager.configure()
            readyHandler?.obtainMessage(R.id.zxing_prewiew_size_ready, previewSize)?.sendToTarget()
        } catch (e: Exception) {
            notifyError(e)
            Log.e(TAG, "Failed to configure camera", e)
        }
    }

    private val previewStarter = Runnable {
        try {
            Log.d(TAG, "Starting preview")
            cameraManager.setPreviewDisplay(surface)
            cameraManager.startPreview()
        } catch (e: Exception) {
            notifyError(e)
            Log.e(TAG, "Failed to start preview", e)
        }
    }

    private val closer = Runnable {
        try {
            Log.d(TAG, "Closing camera")
            cameraManager.stopPreview()
            cameraManager.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close camera", e)
        }
        isCameraClosed = true
        readyHandler?.sendEmptyMessage(R.id.zxing_camera_closed)
        cameraThread.decrementInstances()
    }

    /**
     * Actual preview size in current rotation. null if not determined yet.
     *
     * @return preview size
     */
    private val previewSize: Size?
        get() = cameraManager.getPreviewSize()

    /**
     * @return the camera rotation relative to display rotation, in degrees. Typically 0 if the
     * display is in landscape orientation.
     */
    val cameraRotation: Int
        get() = cameraManager.cameraRotation

    /**
     * Construct a new CameraInstance.
     *
     *
     * A new CameraManager is created.
     *
     * @param context the Android Context
     */
    constructor(context: Context) : this(CameraManager(context))

    fun setReadyHandler(readyHandler: Handler?) {
        this.readyHandler = readyHandler
    }

    fun setSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        surface = CameraSurface(surfaceHolder)
    }

    fun getCameraSettings(): CameraSettings {
        return cameraSettings
    }

    /**
     * This only has an effect if the camera is not opened yet.
     *
     * @param cameraSettings the new camera settings
     */
    fun setCameraSettings(cameraSettings: CameraSettings) {
        if (!isOpen) {
            this.cameraSettings = cameraSettings
            cameraManager.cameraSettings = cameraSettings
        }
    }

    fun open() {
        Util.validateMainThread()
        isOpen = true
        isCameraClosed = false
        cameraThread.incrementAndEnqueue(opener)
    }

    fun configureCamera() {
        Util.validateMainThread()
        validateOpen()
        cameraThread.enqueue(configure)
    }

    fun startPreview() {
        Util.validateMainThread()
        validateOpen()
        cameraThread.enqueue(previewStarter)
    }

    fun setTorch(on: Boolean) {
        Util.validateMainThread()
        if (isOpen) {
            cameraThread.enqueue { cameraManager.setTorch(on) }
        }
    }

    /**
     * Changes the settings for Camera.
     *
     * @param callback [CameraParametersCallback]
     */
    fun changeCameraParameters(callback: CameraParametersCallback) {
        Util.validateMainThread()
        if (isOpen) {
            cameraThread.enqueue { cameraManager.changeCameraParameters(callback) }
        }
    }

    fun close() {
        Util.validateMainThread()
        if (isOpen) {
            cameraThread.enqueue(closer)
        } else {
            isCameraClosed = true
        }
        isOpen = false
    }

    fun requestPreview(callback: PreviewCallback?) {
        mainHandler.post {
            if (!isOpen) {
                Log.d(TAG, "Camera is closed, not requesting preview")
            } else {
                cameraThread.enqueue { cameraManager.requestPreviewFrame(callback) }
            }
        }
    }

    private fun validateOpen() {
        check(isOpen) { "CameraInstance is not open" }
    }

    private fun notifyError(error: Exception) {
        readyHandler?.obtainMessage(R.id.zxing_camera_error, error)?.sendToTarget()
    }

    companion object {
        private val TAG = CameraInstance::class.java.simpleName
    }

    init {
        Util.validateMainThread()
        cameraManager.cameraSettings = cameraSettings
        mainHandler = Handler()
    }
}