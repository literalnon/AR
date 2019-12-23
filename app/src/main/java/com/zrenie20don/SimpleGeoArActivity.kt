package com.zrenie20don


import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.wikitude.architect.ArchitectView
import com.zrenie20don.util.location.LocationProvider
import kotlinx.android.synthetic.main.architect_cam.*

/**
 * This Activity is (almost) the least amount of code required to use the
 * basic functionality for Geo AR.
 *
 * This Activity needs Manifest.permission.ACCESS_FINE_LOCATION permissions
 * in addition to the required permissions of the SimpleArActivity.
 */
open class SimpleGeoArActivity : DonArActivity(), LocationListener {

    protected var currentLocation: Location? = null
    /**
     * Very basic location provider to enable location updates.
     * Please note that this approach is very minimal and we recommend to implement a more
     * advanced location provider for your app. (see https://developer.android.com/training/location/index.html)
     */
    var locationProvider: LocationProvider? = null

    /**
     * Error callback of the LocationProvider, noProvidersEnabled is called when neither location over GPS nor
     * location over the network are enabled by the device.
     */
    private val errorCallback = LocationProvider.ErrorCallback {
        Toast.makeText(this, "no location provider", Toast.LENGTH_LONG).show();
    }

    /**
     * The ArchitectView.SensorAccuracyChangeListener notifies of changes in the accuracy of the compass.
     * This can be used to notify the user that the sensors need to be recalibrated.
     *
     * This listener has to be registered after onCreate and unregistered before onDestroy in the ArchitectView.
     */
    private val sensorAccuracyChangeListener = ArchitectView.SensorAccuracyChangeListener { accuracy ->
        if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) { // UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3
            //Toast.makeText(SimpleGeoArActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationProvider = LocationProvider(this, this, errorCallback)
    }

    override fun onResume() {
        super.onResume()
        locationProvider!!.onResume()
        /*
         * The SensorAccuracyChangeListener has to be registered to the Architect view after ArchitectView.onCreate.
         * There may be more than one SensorAccuracyChangeListener.
         */
        try {
            architectView?.registerSensorAccuracyChangeListener(sensorAccuracyChangeListener)
        } catch (e: Exception) {
            Log.e("architectView", "architectView.registerSensorAccuracyChangeListener()")
            e.printStackTrace()
            Toast.makeText(this, "architectView.registerSensorAccuracyChangeListener ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    override fun onPause() {
        locationProvider!!.onPause()
        super.onPause()
        // The SensorAccuracyChangeListener has to be unregistered from the Architect view before ArchitectView.onDestroy.
        try {
            architectView?.unregisterSensorAccuracyChangeListener(sensorAccuracyChangeListener)
        } catch (e: Exception) {
            Log.e("architectView", "architectView.unregisterSensorAccuracyChangeListener()")
            e.printStackTrace()
            Toast.makeText(this, "architectView.unregisterSensorAccuracyChangeListener ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * The ArchitectView has to be notified when the location of the device
     * changed in order to accurately display the Augmentations for Geo AR.
     *
     * The ArchitectView has two methods which can be used to pass the Location,
     * it should be chosen by whether an altitude is available or not.
     */
    override fun onLocationChanged(location: Location) {
        currentLocation = location

        val accuracy = if (location.hasAccuracy()) location.accuracy else 1000f
        try {
            if (location.hasAltitude()) {
                architectView?.setLocation(location.latitude, location.longitude, location.altitude, accuracy)
            } else {
                architectView?.setLocation(location.latitude, location.longitude, accuracy.toDouble())
            }
        } catch (e: Exception) {
            Log.e("architectView", "architectView.onLocationChanged()")
            e.printStackTrace()
            Toast.makeText(this, "architectView.onLocationChanged ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * The very basic LocationProvider setup of this sample app does not handle the following callbacks
     * to keep the sample app as small as possible. They should be used to handle changes in a production app.
     */
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}
