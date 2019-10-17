package com.zrenie20don

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import android.view.OrientationEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.architect_cam.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class DonArGeoActivity : SimpleGeoArActivity() {

    companion object {
        const val TIME_FORMAT = "HH:mm:ss"
    }

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var recordActivity: ScreenRecordActivity? = null

    lateinit var orientationListener: OrientationEventListener

    var googleApiClient: GoogleApiClient? = null
    val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000 * 6 * 10L
    val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    val REQUEST_CHECK_SETTINGS = 0x1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientationListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(orientation: Int) {
                Log.e("listener", "onOrientationChanged : ${orientation}")

                val newOrientation = when(orientation) {
                    in 0..45 -> {
                        360
                    }
                    in 45..135 -> {
                        270
                    }
                    in 135..225 -> {
                        180
                    }
                    in 225..315 -> {
                        90
                    }
                    in 315..360 -> {
                        360
                    }
                    else -> {
                        360
                    }
                }.toFloat()

                videoButton.rotation = newOrientation
                foto_button.rotation = newOrientation
                //fab.rotation = newOrientation

                webInfoFab.rotation = newOrientation
                informationFab.rotation = newOrientation
                geoArSwitcher.rotation = newOrientation
                flashSwitcher.rotation = newOrientation
                switchCameraFab.rotation = newOrientation
            }
        }

        tvTime?.text = ""

        videoButton?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                recordActivity = ScreenRecordActivity(this, object : ITimeChangedListener {
                    override fun timerStart() {

                        if (fab.visibility == View.VISIBLE) {
                            fab?.visibility = View.INVISIBLE
                            foto_button?.visibility = View.INVISIBLE

                            fab?.isEnabled = false
                            foto_button?.isEnabled = false
                        } else {
                            fabLand?.visibility = View.INVISIBLE
                            foto_buttonLand?.visibility = View.INVISIBLE

                            fabLand?.isEnabled = false
                            foto_buttonLand?.isEnabled = false
                        }

                        tvTime?.visibility = View.VISIBLE
                        tvTime?.text = "00"
                    }

                    override fun timerChange(time: Long) {
                        val hour = time / 60 / 60 % 60
                        val minute = time / 60 % 60
                        val seconds = time % 60

                        val timeStr = when {
                            hour != 0L -> "${hour.getString()}:${minute.getString()}:${seconds.getString()}"
                            minute != 0L -> "${minute.getString()}:${seconds.getString()}"
                            else -> seconds.getString()
                        }

                        tvTime?.text = timeStr
                    }

                    fun Long.getString(): String {
                        return if (this < 10) {
                            "0${this}"
                        } else {
                            "${this}"
                        }
                    }

                    override fun timerEnd() {
                        if (fab.visibility == View.INVISIBLE) {
                            fab?.visibility = View.VISIBLE
                            foto_button?.visibility = View.VISIBLE
                        } else {
                            fabLand?.visibility = View.VISIBLE
                            foto_buttonLand?.visibility = View.VISIBLE
                        }

                        fab?.isEnabled = true
                        foto_button?.isEnabled = true

                        fabLand?.isEnabled = true
                        foto_buttonLand?.isEnabled = true

                        tvTime?.visibility = View.INVISIBLE
                    }

                })

                recordActivity?.onCreate(arrayListOf(videoButton, videoButtonLand))
            } else {
                it.visibility = View.GONE
                videoButtonLand.visibility = View.GONE
            }
        }

        val fotoClickListener = View.OnClickListener {
            try {
                architectView.captureScreen(1) { bitmap ->
                    try {
                        saveImage(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } catch (e: Exception) {
                foto_button.setVisibility(View.GONE)
                foto_buttonLand.setVisibility(View.GONE)
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        foto_button?.setOnClickListener(fotoClickListener)
        foto_buttonLand?.setOnClickListener(fotoClickListener)

        if (scaleGestureDetector == null) {
            scaleGestureDetector = ScaleGestureDetector(applicationContext, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    Log.e("gesture", "detector : ${detector?.isInProgress}, ${detector?.scaleFactor}, ${detector?.timeDelta}, ${detector?.eventTime}, ${detector?.currentSpan}")
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                }

                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    Log.e("gesture", "onScale :  detector?.scaleFactor : ${detector?.scaleFactor}")
                    var scaleFactor = detector?.scaleFactor ?: 1f
                    scaleFactor -= 1
                    scaleFactor *= 100

                    architectView.callJavascript("AR.hardware.camera.zoom = AR.hardware.camera.zoom + ${scaleFactor} * (AR.hardware.camera.features.zoomRange.max - AR.hardware.camera.zoom + 1) / 100;")
                    return true
                }
            })

            zoomView?.setOnTouchListener { v, event ->
                //val archTouch = architectView.onTouchEvent(event)
                //Log.e("gesture", "${archTouch}")
                architectView?.dispatchTouchEvent(event)
                val gesture = scaleGestureDetector?.onTouchEvent(event)
                Log.e("gesture", "${gesture}")
                return@setOnTouchListener gesture ?: false
            }
        }

        var flashIsChecked = true

        val flashSwitcherClickListener = View.OnClickListener {
            architectView.callJavascript("AR.hardware.camera.flashlight = ${flashIsChecked};")
            flashIsChecked = !flashIsChecked
        }

        flashSwitcher?.setOnClickListener(flashSwitcherClickListener)
        flashSwitcherLand?.setOnClickListener(flashSwitcherClickListener)

        var switchCameraIsChecked = true

        val switchCameraFabClickListener = View.OnClickListener {
            val js = if (switchCameraIsChecked) {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.FRONT"
            } else {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.BACK"
            }
            architectView.callJavascript(js)
            switchCameraIsChecked = !switchCameraIsChecked
        }

        switchCameraFab?.setOnClickListener(switchCameraFabClickListener)
        switchCameraFabLand?.setOnClickListener(switchCameraFabClickListener)

        val geoArSwitcherClickListener = View.OnClickListener {
            //architectView.cullingDistance = 800f

            if (SimpleArActivity.currentWorld == ACTIVITY_ARCHITECT_WORLD_URL) {
                //SimpleArActivity.currentWorld = SimpleArActivity.ACTIVITY_ARCHITECT_WORLD_GEO_URL
                ZrenieApp.wikiType = ARGEOCONST.EXTRA_GEO_TYPE
                createLocationRequest()
                Log.i("TAG", "createLocationRequest")
            } else {
                ZrenieApp.wikiType = ARGEOCONST.EXTRA_AR_TYPE
                startActivity(Intent(this, DonArGeoActivity::class.java))
                finish()
                //SimpleArActivity.currentWorld = SimpleArActivity.ACTIVITY_ARCHITECT_WORLD_URL
            }
            //architectView.load(SimpleArActivity.currentWorld)

        }

        geoArSwitcher?.setOnClickListener(geoArSwitcherClickListener)
        geoArSwitcherLand?.setOnClickListener(geoArSwitcherClickListener)

        val informationFabClickListener = View.OnClickListener {
            startActivity(Intent(this@DonArGeoActivity, PreviewActivity::class.java))
        }

        informationFab?.setOnClickListener(informationFabClickListener)
        informationFabLand?.setOnClickListener(informationFabClickListener)

        val webInfoFabClickListener = View.OnClickListener {
            "http://www.zrenie20.info".openUrl()
        }

        webInfoFab?.setOnClickListener(webInfoFabClickListener)
        webInfoFabLand?.setOnClickListener(webInfoFabClickListener)

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    val listener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            //Log.e("listener", "onAccuracyChanged")
        }

        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("listener", "onSensorChanged ${event.sensor.type}")

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recordActivity?.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            Log.e("tag", "onActivityResult ${resultCode}")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recordActivity?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recordActivity?.onDestroy()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.e("onConfigurationChanged", "newConfig.orientation : ${newConfig.orientation}, ${Configuration.ORIENTATION_LANDSCAPE}")

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (foto_button?.visibility == View.INVISIBLE) {
                foto_buttonLand?.visibility = View.INVISIBLE
                fabLand?.visibility = View.INVISIBLE
            } else {
                foto_buttonLand?.visibility = View.VISIBLE
                fabLand?.visibility = View.VISIBLE
            }

            videoButtonLand?.visibility = View.VISIBLE

            foto_button?.visibility = View.GONE
            videoButton?.visibility = View.GONE
            fab?.visibility = View.GONE
        } else {
            if (foto_buttonLand?.visibility == View.INVISIBLE) {
                foto_button?.visibility = View.INVISIBLE
                fab?.visibility = View.INVISIBLE
            } else {
                foto_button?.visibility = View.VISIBLE
                fab?.visibility = View.VISIBLE
            }

            videoButton?.visibility = View.VISIBLE

            foto_buttonLand?.visibility = View.GONE
            videoButtonLand?.visibility = View.GONE
            fabLand?.visibility = View.GONE
        }
    }

    private fun createLocationRequest() {
        /*googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(p0: Bundle?) {
                        Log.i("TAG", "onConnected")
                    }

                    override fun onConnectionSuspended(p0: Int) {
                        googleApiClient?.connect()
                        Log.i("TAG", "onConnectionSuspended")
                    }
                })
                .addOnConnectionFailedListener {
                    Log.i("TAG", "addOnConnectionFailedListener")
                }
                .addApi(LocationServices.API)
                .build()

        googleApiClient?.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()


        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest)
                .setResultCallback { locationSettingsResult ->
                    val status = locationSettingsResult.status

                    Log.i("TAG", "locationSettingsResult.status : ${locationSettingsResult.status}")

                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS -> {
                            Log.i("TAG", "Все настройки местоположений установлены")
                            startActivity(Intent(this@DonArGeoActivity, DonArGeoActivity::class.java))
                            finish()
                        }

                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i("TAG", "Настройки местоположения не установлены. Показать пользоватею диалог" + "для изменения настроек")
                            status.startResolutionForResult(this@DonArGeoActivity, REQUEST_CHECK_SETTINGS)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Log.i("TAG", "Настройки местоположения не могут быть установлены. Диалог не показываетсяс")
                        }
                    }
                }*/
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            startActivity(Intent(this@DonArGeoActivity, DonArGeoActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        orientationListener.enable()
    }

    override fun onPause() {
        super.onPause()

        orientationListener.disable()
    }
}