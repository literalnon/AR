package com.zrenie20don

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import kotlinx.android.synthetic.main.architect_cam.*

class DonArGeoActivity : SimpleGeoArActivity() {

    // Used to detect pinch zoom gesture.
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var recordActivity = RecordActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvTime?.text = ""

        recordActivity.onCreate(videoButton, View.OnClickListener {
            recordActivity.onClick()
        }, {
            tvTime?.text = it
        }, mainLayout)

        foto_button.setOnClickListener(View.OnClickListener {
            try {
                architectView.captureScreen(1) { bitmap ->
                    try {
                        saveImage(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } catch (e: Exception) {
                foto_button.setVisibility(View.INVISIBLE)
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        })

        if (scaleGestureDetector == null) {
            scaleGestureDetector = ScaleGestureDetector(applicationContext, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                }

                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    var scaleFactor = detector?.scaleFactor ?: 1f
                    scaleFactor -= 1
                    scaleFactor *= 100

                    architectView.callJavascript("AR.hardware.camera.zoom = AR.hardware.camera.zoom + ${scaleFactor} * (AR.hardware.camera.features.zoomRange.max - AR.hardware.camera.zoom + 1) / 100;")
                    return true
                }
            })

            zoomView?.setOnTouchListener { v, event ->
                return@setOnTouchListener scaleGestureDetector?.onTouchEvent(event) ?: false
            }
        }

        var flashIsChecked = true

        flashSwitcher?.setOnClickListener {
            architectView.callJavascript("AR.hardware.camera.flashlight = ${flashIsChecked};")
            flashIsChecked = !flashIsChecked
        }

        var switchCameraIsChecked = true

        switchCameraFab?.setOnClickListener {
            val js = if (switchCameraIsChecked) {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.FRONT"
            } else {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.BACK"
            }
            architectView.callJavascript(js)
            switchCameraIsChecked = !switchCameraIsChecked
        }

        geoArSwitcher?.setOnClickListener {
            loadPoiFromJson(currentLocation.latitude, currentLocation.longitude)
        }

        informationFab?.setOnClickListener {
            startActivity(Intent(this@DonArGeoActivity, PreviewActivity::class.java))
        }

        webInfoFab?.setOnClickListener {
            "http://www.zrenie20.info".openUrl()
        }
    }

    private fun String.openUrl() {
        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(this)
            startActivity(i)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector!!.onTouchEvent(event)

        return super.onTouchEvent(event)
    }

    private fun saveImage(finalBitmap: Bitmap) {

        val root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString()
        val myDir = File("$root/saved_images")
        myDir.mkdirs()
        val generator = Random()

        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)

            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    private fun loadPoiFromJson(lat: Double, lon: Double) {
        architectView.callJavascript(
                """AR.context.onLocationChanged = World.locationChanged;""".trimIndent()
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        recordActivity.onDestroy()
    }
}