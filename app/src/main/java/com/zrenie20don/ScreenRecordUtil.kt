package com.zrenie20don

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.ArrayList


@RequiresApi(21)
class ScreenRecordUtil(
        private val activity: AppCompatActivity,
        private val timeChangedListener: ITimeChangedListener
) {
    companion object {
        const val REQUEST_CODE_CAPTURE_PERM = 1298
    }

    private var mMediaProjectionManager: MediaProjectionManager? = null


    fun onCreate(mToggleButton: ArrayList<View>) {
        mMediaProjectionManager = activity.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

        mToggleButton.forEach {
            it.setOnClickListener { v ->
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                                .checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {

                    } else {
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
                                ScreenRecordActivity.REQUEST_PERMISSIONS)
                    }
                } else {
                    startRecording()//onToggleScreenShare()
                }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>,
                                   grantResults: IntArray) {
        when (requestCode) {
            ScreenRecordActivity.REQUEST_PERMISSIONS -> {
                if (grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                }
                return
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (REQUEST_CODE_CAPTURE_PERM === requestCode) {
            if (resultCode == RESULT_OK) {
                mMediaProjection = mMediaProjectionManager?.getMediaProjection(resultCode, intent!!)
                startRecording() // defined below
            } else {
                // user did not grant permissions
            }
        }
    }

    private val VIDEO_MIME_TYPE = "video/avc";
    private val VIDEO_WIDTH = 1280;
    private val VIDEO_HEIGHT = 720;

    private var mMuxerStarted = false
    private var mMediaProjection: MediaProjection? = null
    private var mInputSurface: Surface? = null
    private var mMuxer: MediaMuxer? = null
    private var mVideoEncoder: MediaCodec? = null
    private var  mVideoBufferInfo: MediaCodec.BufferInfo? = null
    private var mTrackIndex = -1

    private val mDrainHandler = Handler(Looper.getMainLooper())
    private var mDrainEncoderRunnable = Runnable {
        drainEncoder()
    }

    var running = false

    private fun startRecording() {
        if (!running) {
            drainEncoder()
            releaseEncoders()
        } else {
            val dm = activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
            val defaultDisplay = dm?.getDisplay(Display.DEFAULT_DISPLAY)
            if (defaultDisplay == null) {
                Toast.makeText(activity, "Error: virtual display is empty", Toast.LENGTH_LONG).show()
            }
            prepareVideoEncoder()

            try {
                mMuxer = MediaMuxer("/sdcard/video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (ioe: Exception) {
                //throw new RuntimeException ("MediaMuxer creation failed", ioe);
            }

            // Get the display size and density.
            val metrics = activity.resources.displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels
            val screenDensity = metrics.densityDpi

            // Start the video input.
            mMediaProjection?.createVirtualDisplay(
                    "Recording Display",
                    screenWidth,
                    screenHeight,
                    screenDensity,
                    0 /* flags */,
                    mInputSurface,
                    null /* callback */,
                    null /* handler */
            )

            // Start the encoders
            drainEncoder()
        }

        running = !running
    }

    private fun prepareVideoEncoder() {
        mVideoBufferInfo = MediaCodec.BufferInfo()
        val format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT)
        val frameRate = 30

        // Set some required properties. The media codec may fail if these aren't defined.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000); // 6Mbps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds between I-frames

        // Create a MediaCodec encoder and configure it. Get a Surface we can use for recording into.
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
            mVideoEncoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mInputSurface = mVideoEncoder?.createInputSurface()
            mVideoEncoder?.start()
        } catch (e: Exception) {
            releaseEncoders()
        }
    }

    private fun drainEncoder(): Boolean {
        mDrainHandler.removeCallbacks(mDrainEncoderRunnable);
        while (true) {
            val bufferIndex = mVideoEncoder?.dequeueOutputBuffer(mVideoBufferInfo!!, 0)

            if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // nothing available yet
                break;
            } else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mTrackIndex >= 0) {
                    //throw new RuntimeException ("format changed twice");
                }
                mTrackIndex = mMuxer?.addTrack(mVideoEncoder?.outputFormat!!) ?: 0
                if (!mMuxerStarted && mTrackIndex >= 0) {
                    mMuxer?.start();
                    mMuxerStarted = true;
                }
            } else if ((bufferIndex ?: 0) < 0) {
                // not sure what's going on, ignore it
            } else {
                val encodedData = mVideoEncoder?.getOutputBuffer(bufferIndex ?: 0)
                if (encodedData == null) {
                    //throw new RuntimeException ("couldn't fetch buffer at index " + bufferIndex);
                    Toast.makeText(activity, "Error: encoded data is empty", Toast.LENGTH_LONG).show()
                }

                if (mVideoBufferInfo?.flags != null && (mVideoBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mVideoBufferInfo?.size = 0
                }

                if (mVideoBufferInfo?.size != 0) {
                    if (mMuxerStarted) {
                        encodedData?.position(mVideoBufferInfo?.offset!!);
                        encodedData?.limit(mVideoBufferInfo!!.offset + mVideoBufferInfo!!.size);
                        mMuxer?.writeSampleData(mTrackIndex, encodedData!!, mVideoBufferInfo!!)
                    } else {
                        Toast.makeText(activity, "Error: recording not started", Toast.LENGTH_LONG).show()
                    }
                }

                mVideoEncoder?.releaseOutputBuffer(bufferIndex!!, false)

                if ((mVideoBufferInfo?.flags ?: 0 and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }

        mDrainHandler.postDelayed(mDrainEncoderRunnable, 10)
        return false
    }

    private fun releaseEncoders() {
        mDrainHandler.removeCallbacks(mDrainEncoderRunnable);
        if (mMuxer != null) {
            if (mMuxerStarted) {
                mMuxer?.stop()
            }
            mMuxer?.release()
            mMuxer = null;
            mMuxerStarted = false;
        }
        if (mVideoEncoder != null) {
            mVideoEncoder?.stop();
            mVideoEncoder?.release();
            mVideoEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface?.release();
            mInputSurface = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection?.stop();
            mMediaProjection = null;
        }
        mVideoBufferInfo = null;
        //mDrainEncoderRunnable = null;
        mTrackIndex = -1;
    }
}
