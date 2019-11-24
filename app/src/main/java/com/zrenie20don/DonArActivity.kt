package com.zrenie20don

import android.content.Context
import android.content.Intent
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
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.GoogleApiClient
import com.wikitude.architect.ArchitectView
import com.wikitude.common.devicesupport.Feature
import kotlinx.android.synthetic.main.architect_cam.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

open class DonArActivity : SimpleArActivity() {

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

                val newOrientation = when (orientation) {
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

                videoButton?.rotation = newOrientation
                foto_button?.rotation = newOrientation
                //fab.rotation = newOrientation

                webInfoFab?.rotation = newOrientation
                informationFab?.rotation = newOrientation
                geoArSwitcher?.rotation = newOrientation
                flashSwitcher?.rotation = newOrientation
                switchCameraFab?.rotation = newOrientation
            }
        }

        tvTime?.text = ""

        videoButton?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                recordActivity = ScreenRecordActivity(this, object : ITimeChangedListener {
                    override fun timerStart() {

                        fab?.visibility = View.INVISIBLE
                        foto_button?.visibility = View.INVISIBLE

                        fab?.isEnabled = false
                        foto_button?.isEnabled = false

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
                            fab?.visibility = View.VISIBLE
                            foto_button?.visibility = View.VISIBLE

                        fab?.isEnabled = true
                        foto_button?.isEnabled = true

                        tvTime?.visibility = View.INVISIBLE
                    }

                })

                recordActivity?.onCreate(arrayListOf(videoButton))
            } else {
                it.visibility = View.GONE
            }
        }

        val fotoClickListener = View.OnClickListener {
            try {
                architectView?.captureScreen(1) { bitmap ->
                    try {
                        saveImage(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } catch (e: Exception) {
                foto_button?.visibility = View.GONE
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        foto_button?.setOnClickListener(fotoClickListener)

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

                    try {
                        architectView?.callJavascript("AR.hardware.camera.zoom = AR.hardware.camera.zoom + ${scaleFactor} * (AR.hardware.camera.features.zoomRange.max - AR.hardware.camera.zoom + 1) / 100;")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, "architectView.callJavascript ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    return true
                }
            })

            zoomView?.setOnTouchListener { v, event ->
                //val archTouch = architectView.onTouchEvent(event)
                //Log.e("gesture", "${archTouch}")
                try {
                    architectView?.dispatchTouchEvent(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "architectView.dispatchTouchEvent ${e.message}", Toast.LENGTH_LONG).show()
                }
                val gesture = scaleGestureDetector?.onTouchEvent(event)
                Log.e("gesture", "${gesture}")
                return@setOnTouchListener gesture ?: false
            }
        }

        var flashIsChecked = true

        val flashSwitcherClickListener = View.OnClickListener {
            architectView?.callJavascript("AR.hardware.camera.flashlight = ${flashIsChecked};")
            flashIsChecked = !flashIsChecked

            if (flashIsChecked) {

            } else {

            }
        }

        flashSwitcher?.setOnClickListener(flashSwitcherClickListener)

        var switchCameraIsChecked = true

        val switchCameraFabClickListener = View.OnClickListener {
            val js = if (switchCameraIsChecked) {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.FRONT"
            } else {
                "AR.hardware.camera.position = AR.CONST.CAMERA_POSITION.BACK"
            }
            architectView?.callJavascript(js)
            switchCameraIsChecked = !switchCameraIsChecked
        }

        switchCameraFab?.setOnClickListener(switchCameraFabClickListener)

        val geoArSwitcherClickListener = View.OnClickListener {
            //architectView.cullingDistance = 800f

            /*if (SimpleArActivity.currentWorld == ACTIVITY_ARCHITECT_WORLD_URL) {
                //SimpleArActivity.currentWorld = SimpleArActivity.ACTIVITY_ARCHITECT_WORLD_GEO_URL
                ZrenieApp.wikiType = ARGEOCONST.EXTRA_GEO_TYPE
                createLocationRequest()
                Log.i("TAG", "createLocationRequest")
            } else {
                ZrenieApp.wikiType = ARGEOCONST.EXTRA_AR_TYPE
                startActivity(Intent(this, DonArActivity::class.java))
                finish()
                //SimpleArActivity.currentWorld = SimpleArActivity.ACTIVITY_ARCHITECT_WORLD_URL
            }*/

            //architectView.load(SimpleArActivity.currentWorld)

            AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title)
                    .setItems(R.array.choice_dialog) { p0, p1 ->
                        when(p1) {
                            0 -> {
                                if (!isSupporting(ARGEOCONST.EXTRA_AR_TYPE)) {
                                    AlertDialog.Builder(this)
                                            .setTitle("Alert!")
                                            .setMessage("Device is not supported!")
                                            .create()
                                            .show()
                                } else {
                                    ZrenieApp.wikiType = ARGEOCONST.EXTRA_AR_TYPE
                                    startActivity(Intent(this, DonArActivity::class.java))
                                    finish()
                                }
                            }
                            1 -> {
                                if (!isSupporting(ARGEOCONST.EXTRA_3D)) {
                                    AlertDialog.Builder(this)
                                            .setTitle("Alert!")
                                            .setMessage("Device is not supported!")
                                            .create()
                                            .show()
                                } else {

                                    ZrenieApp.wikiType = ARGEOCONST.EXTRA_3D
                                    startActivity(Intent(this, DonArActivity::class.java))
                                    finish()
                                }
                            }
                            2 -> {
                                if (!isSupporting(ARGEOCONST.EXTRA_GEO_TYPE)) {
                                    AlertDialog.Builder(this)
                                            .setTitle("Alert!")
                                            .setMessage("Device is not supported!")
                                            .create()
                                            .show()
                                } else {

                                    ZrenieApp.wikiType = ARGEOCONST.EXTRA_GEO_TYPE
                                    createLocationRequest()
                                }
                            }
                        }
                    }
                    .create()
                    .show()
        }

        geoArSwitcher?.setOnClickListener(geoArSwitcherClickListener)
        geoArSwitcher?.setIcon(
                when {
                    ZrenieApp.wikiType == ARGEOCONST.EXTRA_AR_TYPE -> R.drawable.ar_150
                    ZrenieApp.wikiType == ARGEOCONST.EXTRA_3D -> R.drawable.ar3d_150
                    else -> R.drawable.geo_150
                })

        val informationFabClickListener = View.OnClickListener {
            startActivity(Intent(this@DonArActivity, PreviewActivity::class.java))
        }

        informationFab?.setOnClickListener(informationFabClickListener)

        val webInfoFabClickListener = View.OnClickListener {
            "http://www.zrenie20.info".openUrl()
        }

        webInfoFab?.setOnClickListener(webInfoFabClickListener)

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        if (!isSupporting(ZrenieApp.wikiType)) {
            AlertDialog.Builder(this)
                    .setTitle("Alert!")
                    .setMessage("Device is not supported!")
                    .setCancelable(false)
                    .create()
                    .show()
        }
    }

    private fun isSupporting(type: ARGEOCONST): Boolean {
        val features = EnumSet.noneOf(Feature::class.java)

        //features.add(Feature.INSTANT_TRACKING)


        when (type) {
            ARGEOCONST.EXTRA_AR_TYPE -> {
                features.add(Feature.IMAGE_TRACKING)
            }
            ARGEOCONST.EXTRA_GEO_TYPE -> {
                features.add(Feature.GEO)
            }
            ARGEOCONST.EXTRA_3D -> {
                features.add(Feature.OBJECT_TRACKING)
            }
        }

        return ArchitectView.isDeviceSupporting(this, features).isSuccess
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
                            startActivity(Intent(this@DonArActivity, DonArActivity::class.java))
                            finish()
                        }

                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i("TAG", "Настройки местоположения не установлены. Показать пользоватею диалог" + "для изменения настроек")
                            status.startResolutionForResult(this@DonArActivity, REQUEST_CHECK_SETTINGS)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Log.i("TAG", "Настройки местоположения не могут быть установлены. Диалог не показываетсяс")
                        }
                    }
                }*/
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            startActivity(Intent(this@DonArActivity, DonArActivity::class.java))
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