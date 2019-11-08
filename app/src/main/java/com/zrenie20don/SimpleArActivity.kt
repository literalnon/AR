package com.zrenie20don

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import com.crashlytics.android.Crashlytics

import com.wikitude.architect.ArchitectStartupConfiguration
import com.wikitude.architect.ArchitectView
import com.wikitude.common.camera.CameraSettings

import java.io.IOException

import com.zrenie20don.ZrenieApp.wikiType
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.architect_cam.*

/**
 * This Activity is (almost) the least amount of code required to use the
 * basic functionality for Image-/Instant- and Object Tracking.
 *
 * This Activity needs Manifest.permission.CAMERA permissions because the
 * ArchitectView will try to start the camera.
 */
open class SimpleArActivity : AppCompatActivity() {

    /**
     * The ArchitectView is the core of the AR functionality, it is the main
     * interface to the Wikitude SDK.
     * The ArchitectView has its own lifecycle which is very similar to the
     * Activity lifecycle.
     * To ensure that the ArchitectView is functioning properly the following
     * methods have to be called:
     * - onCreate(ArchitectStartupConfiguration)
     * - onPostCreate()
     * - onResume()
     * - onPause()
     * - onDestroy()
     * Those methods are preferably called in the corresponding Activity lifecycle callbacks.
     */
    protected var config: ArchitectStartupConfiguration? = null
    var architectView: ArchitectView? = null
    /** The path to the AR-Experience. This is usually the path to its index.html.  */
    private val arExperience: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Fabric.with(applicationContext, Crashlytics())
        try {
            super.onCreate(savedInstanceState)

            WebView.setWebContentsDebuggingEnabled(true)

            config = ArchitectStartupConfiguration() // Creates a config with its default values.

            config?.licenseKey = WikitudeSDKConstants.WIKITUDE_SDK_KEY
            config?.features = ArchitectStartupConfiguration.Features.Geo
            config?.cameraResolution = CameraSettings.CameraResolution.AUTO
            config?.cameraFocusMode = CameraSettings.CameraFocusMode.CONTINUOUS
            config?.isCamera2Enabled = true
        } catch (e: java.lang.Exception) {
            Toast.makeText(applicationContext, "onCreate ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            architectView = ArchitectView(this)
            architectView?.onCreate(config)
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.onCreate(config)")
            e.printStackTrace()
            try {
                Toast.makeText(applicationContext, "architectView?.onCreate ${e.message}", Toast.LENGTH_LONG).show()
                architectView?.onCreate(config)
            } catch (e: java.lang.Exception) {
                Toast.makeText(applicationContext, "architectView?.onCreate ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        try {
            currentWorld = if (wikiType === ARGEOCONST.EXTRA_AR_TYPE) {
                ACTIVITY_ARCHITECT_WORLD_URL
            } else if (wikiType === ARGEOCONST.EXTRA_GEO_TYPE) {
                ACTIVITY_ARCHITECT_WORLD_GEO_URL
            } else {
                ACTIVITY_ARCHITECT_WORLD_3D_URL
            }
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.load")
            e.printStackTrace()
        }

        setContentView(R.layout.architect_cam)
        architectViewLayout?.addView(architectView)

    }

    override fun onLowMemory() {
        super.onLowMemory()

        try {
            architectView?.onLowMemory()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "architectView?.onLowMemory ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        try {
            architectView?.onPostCreate()
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.onPostCreate()")
            e.printStackTrace()
            Toast.makeText(applicationContext, "architectView?.onPostCreate ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            architectView?.load(currentWorld)
        } catch (e: java.lang.Exception) {
            Toast.makeText(applicationContext, "architectView?.load ${currentWorld}, ${e.message} ", Toast.LENGTH_LONG).show()
        }
        try {
            architectView?.registerWorldLoadedListener(object : ArchitectView.ArchitectWorldLoadedListener {
                override fun worldWasLoaded(p0: String?) {
                    Toast.makeText(this@SimpleArActivity, "world loaded success", Toast.LENGTH_LONG).show()
                }

                override fun worldLoadFailed(p0: Int, p1: String?, p2: String?) {
                    Toast.makeText(this@SimpleArActivity, "failed! $p0! ${p1}! ${p2}", Toast.LENGTH_LONG).show()
                    try {
                        architectView?.load(currentWorld)
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(applicationContext, "architectView?.load ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "architectView?.registerWorldLoadedListener ${e.message}", Toast.LENGTH_LONG).show()
            //Toast.makeText(this, getString(R.string.error_loading_ar_experience), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience $arExperience.", e)
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            architectView?.onResume(); // Mandatory ArchitectView lifecycle call
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.onResume()")
            e.printStackTrace()
            Toast.makeText(applicationContext, "architectView?.onResume ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            architectView?.onPause(); // Mandatory ArchitectView lifecycle call
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.onPause()")
            e.printStackTrace()

            Toast.makeText(applicationContext, "architectView?.onPause ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            architectView?.clearCache()
            architectView?.onDestroy()
        } catch (e: Exception) {
            Log.e("architectView?", "architectView?.clearCache()")
            e.printStackTrace()
            Toast.makeText(applicationContext, "architectView?.onDestroy ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    companion object {

        val INTENT_EXTRAS_KEY_SAMPLE = "sampleData"

        val ACTIVITY_ARCHITECT_WORLD_URL = "https://hb.bizmrg.com/image-target/experience/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/experience/index.html";
        val ACTIVITY_ARCHITECT_WORLD_GEO_URL = "https://hb.bizmrg.com/geo-target/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/geo/index.html";
        val ACTIVITY_ARCHITECT_WORLD_3D_URL = "https://hb.bizmrg.com/obj-target/experience/index.html"

        var currentWorld = ACTIVITY_ARCHITECT_WORLD_URL

        private val TAG = SimpleArActivity::class.java.simpleName
    }
}
