package com.zrenie20don

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View.inflate
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import com.crashlytics.android.Crashlytics

import com.wikitude.architect.ArchitectStartupConfiguration
import com.wikitude.architect.ArchitectView
import com.wikitude.common.camera.CameraSettings
import com.wikitude.common.devicesupport.Feature
import com.wikitude.common.permission.PermissionManager

import java.io.IOException

import com.zrenie20don.ZrenieApp.wikiType
import com.zrenie20don.extention.CustomArView
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
    var architectView: CustomArView? = null
    /** The path to the AR-Experience. This is usually the path to its index.html.  */
    private val arExperience: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("lifecycle", "onCreate")
        try {
            super.onCreate(savedInstanceState)

            Fabric.with(this, Crashlytics())

            WebView.setWebContentsDebuggingEnabled(true)

            config = ArchitectStartupConfiguration() // Creates a config with its default values.

            config?.licenseKey = WikitudeSDKConstants.WIKITUDE_SDK_KEY

            config?.features = when (ZrenieApp.wikiType) {
                ARGEOCONST.EXTRA_AR_TYPE -> {
                    ArchitectStartupConfiguration.Features.ImageTracking
                }
                ARGEOCONST.EXTRA_GEO_TYPE -> {
                    ArchitectStartupConfiguration.Features.Geo
                }
                ARGEOCONST.EXTRA_3D -> {
                    ArchitectStartupConfiguration.Features.ObjectTracking
                }
            }
            //config?.features = ArchitectStartupConfiguration.Features.Geo
            //config?.cameraResolution = CameraSettings.CameraResolution.AUTO
            //config?.cameraFocusMode = CameraSettings.CameraFocusMode.CONTINUOUS
            //config?.isCamera2Enabled = true
        } catch (e: java.lang.Exception) {
            Toast.makeText(applicationContext, "onCreate ${e.message}", Toast.LENGTH_LONG).show()
        }

        try {
            architectView = CustomArView(this)//ArchitectView(this)
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
            //Log.e("architectView?", "architectView?.load")
            e.printStackTrace()
        }

        val view = layoutInflater.inflate(R.layout.architect_cam, null)
        view.findViewById<FrameLayout>(R.id.architectViewLayout)?.addView(architectView)

        setContentView(view)//R.layout.architect_cam)
        //architectView?.addView(inflate(this, R.layout.architect_cam, null))
        //setContentView(architectView)
        //architectViewLayout?.addView(architectView)
    }

    override fun onLowMemory() {
        Log.e("lifecycle", "onLowMemory")
        super.onLowMemory()

        try {
            architectView?.onLowMemory()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "architectView?.onLowMemory ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        Log.e("lifecycle", "onPostCreate")
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
                    Log.e("lifecycle", "worldWasLoaded")
                   // Toast.makeText(this@SimpleArActivity, "world loaded success", Toast.LENGTH_LONG).show()
                }

                override fun worldLoadFailed(p0: Int, p1: String?, p2: String?) {
                    Log.e("lifecycle", "worldLoadFailed")
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
        Log.e("lifecycle", "onResume")
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
        Log.e("lifecycle", "onPause")
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
        Log.e("lifecycle", "onDestroy")
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

        val ACTIVITY_ARCHITECT_WORLD_URL = "https://storage.cloud.croc.ru:443/zrenie.kudinov/experience/index.html"//"https://hb.bizmrg.com/image-target/experience/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/experience/index.html";
        val ACTIVITY_ARCHITECT_WORLD_GEO_URL = "https://storage.cloud.croc.ru:443/zrenie.kudinov/experience3/index.html"//"https://hb.bizmrg.com/geo-target/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/geo/index.html";
        val ACTIVITY_ARCHITECT_WORLD_3D_URL = "https://storage.cloud.croc.ru:443/zrenie.kudinov/experience2/index.html"//"https://hb.bizmrg.com/obj-target/experience/index.html"

        var currentWorld = ACTIVITY_ARCHITECT_WORLD_URL

        private val TAG = SimpleArActivity::class.java.simpleName
    }
}
