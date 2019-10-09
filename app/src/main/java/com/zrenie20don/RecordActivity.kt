package com.zrenie20don

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ShortBuffer

import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameFilter
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameFilter
import java.io.File
import java.util.*

typealias OnTimeChangeListener = (String) -> Unit

class RecordActivity {

    companion object {
        private const val RECORD_REQUEST_CODE = 101
        private const val STORAGE_REQUEST_CODE = 102
        private const val AUDIO_REQUEST_CODE = 103
    }

    private var mediaProjection: MediaProjection? = null
    private lateinit var mediaRecorder: MediaRecorder
    private var virtualDisplay: VirtualDisplay? = null

    private var running = false
    private var width = 720
    private var height = 1080
    private var dpi: Int = 0
    private var projectionManager: MediaProjectionManager? = null
    private var activity: Activity? = null

    @RequiresApi(21)
    fun onCreate(activity: Activity) {
        running = false

        mediaRecorder = MediaRecorder()
        //initRecorder()

        this.activity = activity
        projectionManager = activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        //val metrics = DisplayMetrics()
        //activity.windowManager.defaultDisplay.getMetrics(metrics)
        //setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
    }

    @RequiresApi(21)
    fun click() {
        activity?.let { activity ->
            if (isRunning()) {
                stopRecord()
            } else {
                val captureIntent = projectionManager!!.createScreenCaptureIntent()
                activity.startActivityForResult(captureIntent, RECORD_REQUEST_CODE)

                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                    )
                } else {
                    //val captureIntent = projectionManager?.createScreenCaptureIntent()
                    //activity.startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
                }
            }
        }
    }

    private fun isRunning(): Boolean {
        return running
    }

    @RequiresApi(21)
    private fun startRecord(): Boolean {
        if (mediaProjection == null || running) {
            //return false;
            throw RuntimeException("mediaProjection == null || running")
        }

        initRecorder()

        mediaRecorder.start()
        running = true

        return true
    }

    @RequiresApi(21)
    private fun stopRecord(): Boolean {
        if (!running) {
            return false
        }

        running = false
        try {
            mediaRecorder.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaRecorder.reset()
        mediaProjection!!.stop()
        virtualDisplay!!.release()

        return true
    }

    @RequiresApi(21)
    private fun createVirtualDisplay() {
        virtualDisplay = mediaProjection!!.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.surface, null, null);
    }

    @RequiresApi(21)
    private fun initRecorder() {
        //val path = getSaveDirectory() + System.currentTimeMillis() + ".mp4"
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        val path = getSaveDirectory() + System.currentTimeMillis() + ".mp4"
        Log.e("mediaRecorder", "path" + path)
        mediaRecorder.setOutputFile(path)
        mediaRecorder.setVideoSize(width, height)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024)
        mediaRecorder.setVideoFrameRate(30)

        val metrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels
        dpi = metrics.densityDpi

        Log.e("MediaRecorder", "width : ${width}, height : ${height}, dpi : ${dpi}")

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)

        val profile: CamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        profile.videoFrameHeight = height//metrics.heightPixels
        profile.videoFrameWidth = width//metrics.widthPixels

        mediaRecorder.setProfile(profile)
        //mediaRecorder.setOutputFile(path)
        mediaRecorder.prepare()

        virtualDisplay = mediaProjection!!.createVirtualDisplay("MainScreen",
                width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.surface, null, null)
        /*try {
            mediaRecorder.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/
    }

    private fun getSaveDirectory(): String? {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val rootDir = Environment.getExternalStorageDirectory().absolutePath + "/" + "ScreenRecord" + "/"

            val file = File(rootDir)

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null
                }
            }

            return rootDir
        } else {
            return null
        }
    }

    @RequiresApi(21)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.e("mediaRecorder", "startRecord")
            mediaProjection = projectionManager!!.getMediaProjection(resultCode, data)
            startRecord()
            //handler!!
        }
    }

    @RequiresApi(21)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                click()
            }
        }
    }

    fun onDestroy() {
        activity = null
    }
}

/*class RecordActivity {

    private val ffmpeg_link = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES).toString() + "${Date().time}.flv"//"/mnt/sdcard/stream.flv"

    var startTime: Long = 0
    var recording = false

    var timeUpdateRunnable: Runnable? = null
    val timeUpdateHandler = Handler()

    var recorder: FFmpegFrameRecorder? = null

    private var isPreviewOn = false

*//*Filter information, change boolean to true if adding a fitler*//*

    private val addFilter = true
    private var filterString = ""
    lateinit var filter: FFmpegFrameFilter

    private val sampleAudioRateInHz = 44100
    private var imageWidth = 320
    private var imageHeight = 240
    private val frameRate = 30

*//* audio data getting thread *//*

    private var audioRecord: AudioRecord? = null
    private var audioRecordRunnable: AudioRecordRunnable? = null
    private var audioThread: Thread? = null
    @Volatile
    internal var runAudioThread = true

*//* video data getting thread *//*

    private var cameraDevice: Camera? = null
    private var cameraView: CameraView? = null

    private var yuvImage: Frame? = null

*//* layout setting *//*

    private val bg_screen_bx = 232
    private val bg_screen_by = 128
    private val bg_screen_width = 700
    private val bg_screen_height = 500
    private val bg_width = 1123
    private val bg_height = 715
    private val live_width = 640
    private val live_height = 480
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

*//* The number of seconds in the continuous record loop (or 0 to disable loop). *//*

    internal val RECORD_LENGTH = 0
    lateinit var images: Array<Frame>
    lateinit var timestamps: LongArray
    lateinit var samples: Array<ShortBuffer>
    internal var imagesIndex: Int = 0
    internal var samplesIndex: Int = 0

    lateinit var onTimeChangeListener: OnTimeChangeListener

    fun onCreate(clickView: View, clickListener: OnClickListener, onTimeChangeListener: OnTimeChangeListener, topLayout: ViewGroup) {
*//*super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
*//*

        this.onTimeChangeListener = onTimeChangeListener
        timeUpdateRunnable = Runnable {
            onTimeChangeListener((System.currentTimeMillis() - startTime).toString())
            timeUpdateHandler.postDelayed(timeUpdateRunnable, 1000)
        }

        initLayout(clickView, clickListener, topLayout)
    }

    fun onDestroy() {

        timeUpdateHandler.removeCallbacks(timeUpdateRunnable)
        recording = false

*//*if (cameraView != null) {
            cameraView!!.stopPreview()
        }*//*


        if (cameraDevice != null) {
            cameraDevice!!.stopPreview()
            cameraDevice!!.release()
            cameraDevice = null
        }
    }

    private fun initLayout(btnRecorderControl: View, clickListener: OnClickListener, topLayout: ViewGroup) {

*//* get size of screen *//*

        val context = btnRecorderControl.context

        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        screenWidth = display.width
        screenHeight = display.height

*//* add control button: start and stop *//*

        btnRecorderControl.setOnClickListener(clickListener)

*//* add camera view *//*

        val display_width_d = (1.0 * bg_screen_width.toDouble() * screenWidth.toDouble() / bg_width).toInt()
        val display_height_d = (1.0 * bg_screen_height.toDouble() * screenHeight.toDouble() / bg_height).toInt()

        cameraDevice = Camera.open()
        Log.i(LOG_TAG, "cameara open")
        cameraView = CameraView(context, cameraDevice)

        topLayout.addView(cameraView, screenWidth, screenHeight)
        //topLayout.addView(cameraView, layoutParam)
        Log.i(LOG_TAG, "cameara preview start: OK")
    }

    //---------------------------------------
    // initialize ffmpeg_recorder
    //---------------------------------------
    private fun initRecorder() {

        Log.w(LOG_TAG, "init recorder")

        Log.i(LOG_TAG, "ffmpeg_url: $ffmpeg_link")
        recorder = FFmpegFrameRecorder(ffmpeg_link, imageWidth, imageHeight, 1)
        recorder!!.format = "flv"
        recorder!!.sampleRate = sampleAudioRateInHz
        // Set in the surface changed method
        recorder!!.frameRate = frameRate.toDouble()

        // The filterString  is any ffmpeg filter.
        // Here is the link for a list: https://ffmpeg.org/ffmpeg-filters.html
        filterString = "transpose=2,crop=w=200:h=200:x=0:y=0"
        filter = FFmpegFrameFilter(filterString, imageWidth, imageHeight)

        //default format on android
        filter.pixelFormat = avutil.AV_PIX_FMT_NV21

        if (RECORD_LENGTH > 0) {
            imagesIndex = 0
            images = arrayOf()//arrayOfNulls(RECORD_LENGTH * frameRate)
            timestamps = LongArray(images.size)
            for (i in images.indices) {
                images[i] = Frame(imageWidth, imageHeight, Frame.DEPTH_UBYTE, 2)
                timestamps[i] = -1
            }
        } else if (yuvImage == null) {
            yuvImage = Frame(imageWidth, imageHeight, Frame.DEPTH_UBYTE, 2)
            Log.i(LOG_TAG, "create yuvImage")
        }

        Log.i(LOG_TAG, "recorder initialize success")

        audioRecordRunnable = AudioRecordRunnable()
        audioThread = Thread(audioRecordRunnable)
        runAudioThread = true
    }

    fun startRecording() {

        initRecorder()

        try {
            recorder!!.start()
            startTime = System.currentTimeMillis()
            recording = true
            audioThread!!.start()

            if (addFilter) {
                filter.start()
            }

            timeUpdateHandler.postDelayed(timeUpdateRunnable, 1000)

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: FrameFilter.Exception) {
            e.printStackTrace()
        }

    }

    fun stopRecording() {

        timeUpdateHandler.removeCallbacks(timeUpdateRunnable)

        runAudioThread = false
        try {
            audioThread!!.join()
        } catch (e: InterruptedException) {
            // reset interrupt to be nice
            Thread.currentThread().interrupt()
            return
        }

        audioRecordRunnable = null
        audioThread = null

        if (recorder != null && recording) {
            if (RECORD_LENGTH > 0) {
                Log.v(LOG_TAG, "Writing frames")
                try {
                    var firstIndex = imagesIndex % samples.size
                    var lastIndex = (imagesIndex - 1) % images.size
                    if (imagesIndex <= images.size) {
                        firstIndex = 0
                        lastIndex = imagesIndex - 1
                    }
                    startTime = timestamps[lastIndex] - RECORD_LENGTH * 1000000L

                    if (startTime < 0) {
                        startTime = 0
                    }

                    if (lastIndex < firstIndex) {
                        lastIndex += images.size
                    }
                    for (i in firstIndex..lastIndex) {
                        val t = timestamps[i % timestamps.size] - startTime
                        if (t >= 0) {
                            if (t > recorder!!.timestamp) {
                                recorder!!.timestamp = t
                            }
                            recorder!!.record(images[i % images.size])
                        }
                    }

                    firstIndex = samplesIndex % samples.size
                    lastIndex = (samplesIndex - 1) % samples.size
                    if (samplesIndex <= samples.size) {
                        firstIndex = 0
                        lastIndex = samplesIndex - 1
                    }
                    if (lastIndex < firstIndex) {
                        lastIndex += samples.size
                    }
                    for (i in firstIndex..lastIndex) {
                        recorder!!.recordSamples(samples[i % samples.size])
                    }
                } catch (e: Exception) {
                    Log.v(LOG_TAG, e.message)
                    e.printStackTrace()
                }

            }

            recording = false
            Log.v(LOG_TAG, "Finishing recording, calling stop and release on recorder")
            try {
                recorder!!.stop()
                recorder!!.release()
                filter.stop()
                filter.release()
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: FrameFilter.Exception) {
                e.printStackTrace()
            }

            recorder = null

        }
    }

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    internal inner class AudioRecordRunnable : Runnable {

        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            // Audio
            val bufferSize: Int
            var audioData: ShortBuffer? = null
            var bufferReadResult: Int

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

            if (RECORD_LENGTH > 0) {
                samplesIndex = 0
                samples = arrayOf()//arrayOfNulls(RECORD_LENGTH * sampleAudioRateInHz * 2 / bufferSize + 1)
                for (i in samples.indices) {
                    samples[i] = ShortBuffer.allocate(bufferSize)
                }
            } else {
                audioData = ShortBuffer.allocate(bufferSize)
            }

            Log.d(LOG_TAG, "audioRecord.startRecording()")
            audioRecord?.startRecording()

*//* ffmpeg_audio encoding loop *//*

            while (runAudioThread) {
                if (RECORD_LENGTH > 0) {
                    audioData = samples[samplesIndex++ % samples.size]
                    audioData.position(0).limit(0)
                }
                //Log.v(LOG_TAG,"recording? " + recording);
                bufferReadResult = audioRecord!!.read(audioData!!.array(), 0, audioData.capacity())
                audioData.limit(bufferReadResult)
                if (bufferReadResult > 0) {
                    Log.v(LOG_TAG, "bufferReadResult: $bufferReadResult")
                    // If "recording" isn't true when start this thread, it never get's set according to this if statement...!!!
                    // Why?  Good question...
                    if (recording) {
                        if (RECORD_LENGTH <= 0)
                            try {
                                recorder!!.recordSamples(audioData)
                                //Log.v(LOG_TAG,"recording " + 1024*i + " to " + 1024*i+1024);
                            } catch (e: Exception) {
                                Log.v(LOG_TAG, e.message)
                                e.printStackTrace()
                            }

                    }
                }
            }
            Log.v(LOG_TAG, "AudioThread Finished, release audioRecord")

*//* encoding finish, release recorder *//*

            if (audioRecord != null) {
                audioRecord!!.stop()
                audioRecord!!.release()
                audioRecord = null
                Log.v(LOG_TAG, "audioRecord released")
            }
        }
    }

    //---------------------------------------------
    // camera thread, gets and encodes video data
    //---------------------------------------------
    internal inner class CameraView(context: Context, private var mCamera: Camera?) : SurfaceView(context), SurfaceHolder.Callback, PreviewCallback {

        private val mHolder: SurfaceHolder

        init {
            Log.w("camera", "camera view")
            mHolder = holder
            mHolder.addCallback(this@CameraView)
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            mCamera!!.setPreviewCallback(this@CameraView)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                stopPreview()
                mCamera!!.setPreviewDisplay(holder)
            } catch (exception: IOException) {
                mCamera!!.release()
                mCamera = null
            }

        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            stopPreview()

            val camParams = mCamera?.parameters
            val sizes = camParams?.supportedPreviewSizes ?: listOf()
            // Sort the list in ascending order
            Collections.sort(sizes) { a, b -> a.width * a.height - b.width * b.height }

            // Pick the first preview size that is equal or bigger, or pick the last (biggest) option if we cannot
            // reach the initial settings of imageWidth/imageHeight.
            for (i in sizes.indices) {
                if (sizes[i].width >= imageWidth && sizes[i].height >= imageHeight || i == sizes.size - 1) {
                    imageWidth = sizes[i].width
                    imageHeight = sizes[i].height
                    Log.v(LOG_TAG, "Changed to supported resolution: " + imageWidth + "x" + imageHeight)
                    break
                }
            }
            camParams?.setPreviewSize(imageWidth, imageHeight)

            Log.v(LOG_TAG, "Setting imageWidth: $imageWidth imageHeight: $imageHeight frameRate: $frameRate")

            camParams?.previewFrameRate = frameRate
            Log.v(LOG_TAG, "Preview Framerate: " + camParams?.previewFrameRate)

            mCamera!!.parameters = camParams

            // Set the holder (which might have changed) again
            try {
                mCamera!!.setPreviewDisplay(holder)
                mCamera!!.setPreviewCallback(this@CameraView)
                startPreview()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Could not set preview display in surfaceChanged")
            }

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            try {
                mHolder.addCallback(null)
                mCamera!!.setPreviewCallback(null)
            } catch (e: RuntimeException) {
                // The camera has probably just been released, ignore.
            }

        }

        fun startPreview() {
            if (!isPreviewOn && mCamera != null) {
                isPreviewOn = true
                mCamera!!.startPreview()
            }
        }

        fun stopPreview() {
            if (isPreviewOn && mCamera != null) {
                isPreviewOn = false
                mCamera!!.stopPreview()
            }
        }

        override fun onPreviewFrame(data: ByteArray, camera: Camera) {
            if (audioRecord == null || audioRecord!!.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                startTime = System.currentTimeMillis()
                return
            }
            if (RECORD_LENGTH > 0) {
                val i = imagesIndex++ % images.size
                yuvImage = images[i]
                timestamps[i] = 1000 * (System.currentTimeMillis() - startTime)
            }


*//* get video data *//*

            if (yuvImage != null && recording) {
                (yuvImage!!.image[0].position(0) as ByteBuffer).put(data)

                if (RECORD_LENGTH <= 0)
                    try {
                        Log.v(LOG_TAG, "Writing Frame")
                        val t = 1000 * (System.currentTimeMillis() - startTime)
                        if (t > recorder!!.timestamp) {
                            recorder!!.timestamp = t
                        }

                        if (addFilter) {
                            filter.push(yuvImage!!)
                            var frame2: Frame? = null
                            frame2 = filter.pull()

                            while (frame2 != null) {
                                recorder!!.record(frame2, filter.pixelFormat)
                                frame2 = filter.pull()
                            }
                        } else {
                            recorder!!.record(yuvImage!!)
                        }
                    } catch (e: Exception) {
                        Log.v(LOG_TAG, e.message)
                        e.printStackTrace()
                    } catch (e: FrameFilter.Exception) {
                        Log.v(LOG_TAG, e.message)
                        e.printStackTrace()
                    }

            }
        }
    }

    fun onClick() {
        if (!recording) {
            startRecording()
            Log.w(LOG_TAG, "Start Button Pushed")
        } else {
            // This will trigger the audio recording loop to stop and then set isRecorderStart = false;
            stopRecording()
            Log.w(LOG_TAG, "Stop Button Pushed")
        }
    }

    companion object {

        private val CLASS_LABEL = "RecordActivity"
        private val LOG_TAG = CLASS_LABEL
    }
}*/
