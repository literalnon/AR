package com.zrenie20don

import android.Manifest
import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

interface ITimeChangedListener{
    fun timerStart()
    fun timerChange(time: Long)
    fun timerEnd()
}

@RequiresApi(21)
class ScreenRecordActivity(
        private val activity: AppCompatActivity,
        private val timeChangedListener: ITimeChangedListener
) {
    private var mScreenDensity: Int = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    private var mToggleButton: ToggleButton? = null
    private var mMediaRecorder: MediaRecorder? = null
    private val handler = Handler()
    private var runnable: Runnable? = null
    private var timer = 0L

    fun onCreate(mToggleButton: ArrayList<View>) {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mMediaRecorder = MediaRecorder()
        mProjectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        runnable = Runnable {
            timer += 1
            timeChangedListener.timerChange(timer)
            handler.postDelayed(runnable, 1000)
        }

        mToggleButton.forEach {
            it.setOnClickListener { v ->
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                                .checkSelfPermission(activity,
                                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {

                    } else {
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
                                REQUEST_PERMISSIONS)
                    }
                } else {
                    onToggleScreenShare()
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE) {
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        mMediaProjectionCallback = MediaProjectionCallback()
        mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data!!)
        mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder!!.start()
    }

    var running = false

    fun onToggleScreenShare() {
        if (!running) {
            initRecorder()
            shareScreen()

            timer = 0
            timeChangedListener.timerStart()
            handler.postDelayed(runnable, 1000)
        } else {
            try {
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopScreenSharing()

            timeChangedListener.timerEnd()
            handler.removeCallbacks(runnable)
        }

        running = !running
    }

    private fun shareScreen() {
        if (mMediaProjection == null) {
            activity.startActivityForResult(mProjectionManager!!.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder!!.start()
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder!!.surface, null, null
        )
    }

    private fun initRecorder() {
        try {
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mMediaRecorder!!.setOutputFile(Environment
                    .getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS).toString() + "/video${Calendar.getInstance().timeInMillis}.mp4")
            mMediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder!!.setVideoEncodingBitRate(512 * 1000)
            mMediaRecorder!!.setVideoFrameRate(30)
            val rotation = activity.windowManager.defaultDisplay.rotation
            val orientation = ORIENTATIONS.get(rotation + 90)
            mMediaRecorder!!.setOrientationHint(orientation)
            mMediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            if (mToggleButton!!.isChecked) {
                mToggleButton!!.isChecked = false
                mMediaRecorder!!.stop()
                mMediaRecorder!!.reset()
            }
            mMediaProjection = null
            stopScreenSharing()
        }
    }

    private fun stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        destroyMediaProjection()
    }

    fun onDestroy() {
        destroyMediaProjection()
    }

    private fun destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare()
                }
                return
            }
        }
    }

    companion object {
        private val TAG = "MainActivity"
        private val REQUEST_CODE = 1000
        private val DISPLAY_WIDTH = 720
        private val DISPLAY_HEIGHT = 1280
        private val ORIENTATIONS = SparseIntArray()
        private val REQUEST_PERMISSIONS = 10

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }
}
