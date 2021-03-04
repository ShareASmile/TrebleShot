package com.journeyapps.barcodescanner.camera

import android.os.Handler
import android.os.HandlerThread

/**
 * Singleton thread that is started and stopped on demand.
 *
 *
 * Any access to Camera / CameraManager should happen on this thread, through CameraInstance.
 */
open class CameraThread private constructor() {
    private val lock = Any()

    private var handler: Handler? = null

    private var thread: HandlerThread? = null

    private var openCount = 0

    /**
     * Call from main thread or camera thread.
     *
     * Enqueues a task on the camera thread.
     *
     * @param runnable the task to enqueue
     */
    fun enqueue(runnable: Runnable) {
        synchronized(lock) {
            checkRunning()
            handler?.post(runnable)
        }
    }

    /**
     * Call from main thread or camera thread.
     *
     * Enqueues a task on the camera thread.
     *
     * @param runnable    the task to enqueue
     * @param delayMillis the delay in milliseconds before executing the runnable
     */
    protected fun enqueueDelayed(runnable: Runnable, delayMillis: Long) {
        synchronized(lock) {
            checkRunning()
            handler?.postDelayed(runnable, delayMillis)
        }
    }

    private fun checkRunning() {
        synchronized(lock) {
            if (handler == null) {
                check(openCount > 0) { "CameraThread is not open" }
                thread = HandlerThread("CameraThread").also {
                    it.start()
                    handler = Handler(it.looper)
                }
            }
        }
    }

    /**
     * Call from camera thread.
     */
    private fun quit() {
        synchronized(lock) {
            thread?.let {
                it.quit()
                thread = null
                handler = null
            }
        }
    }

    /**
     * Call from camera thread
     */
    fun decrementInstances() {
        synchronized(lock) {
            openCount -= 1
            if (openCount == 0) {
                quit()
            }
        }
    }

    /**
     * Call from main thread.
     *
     * @param runner The [Runnable] to be enqueued
     */
    fun incrementAndEnqueue(runner: Runnable) {
        synchronized(lock) {
            openCount += 1
            enqueue(runner)
        }
    }

    companion object {
        private val TAG = CameraThread::class.simpleName

        val instance: CameraThread by lazy {
            CameraThread()
        }
    }
}