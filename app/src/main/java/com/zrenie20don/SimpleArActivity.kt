package com.zrenie20don

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.webkit.WebView
import android.widget.Toast

import com.wikitude.architect.ArchitectStartupConfiguration
import com.wikitude.architect.ArchitectView
import com.wikitude.common.camera.CameraSettings

import java.io.IOException

import com.zrenie20don.ZrenieApp.wikiType
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

    /** The path to the AR-Experience. This is usually the path to its index.html.  */
    private val arExperience: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.architect_cam)

        // Used to enabled remote debugging of the ArExperience with google chrome https://developers.google.com/web/tools/chrome-devtools/remote-debugging
        WebView.setWebContentsDebuggingEnabled(true)
        architectView
        /*
         * The following code is used to run different configurations of the SimpleArActivity,
         * it is not required to use the ArchitectView but is used to simplify the Sample App.
         *
         * Because of this the Activity has to be started with correct intent extras.
         * e.g.:
         *  SampleData sampleData = new SampleData.Builder("SAMPLE_NAME", "PATH_TO_AR_EXPERIENCE")
         *              .arFeatures(ArchitectStartupConfiguration.Features.ImageTracking)
         *              .cameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS)
         *              .cameraPosition(CameraSettings.CameraPosition.BACK)
         *              .cameraResolution(CameraSettings.CameraResolution.HD_1280x720)
         *              .camera2Enabled(false)
         *              .build();
         *
         * Intent intent = new Intent(this, SimpleArActivity.class);
         * intent.putExtra(UrlLauncherStorageActivity.URL_LAUNCHER_SAMPLE_CATEGORY, category);
         * startActivity(intent);
         */

        /*
         * The ArchitectStartupConfiguration is required to call architectView.onCreate.
         * It controls the startup of the ArchitectView which includes camera settings,
         * the required device features to run the ArchitectView and the LicenseKey which
         * has to be set to enable an AR-Experience.
         */
        config = ArchitectStartupConfiguration() // Creates a config with its default values.

        config?.licenseKey = WikitudeSDKConstants.WIKITUDE_SDK_KEY
        config?.features = ArchitectStartupConfiguration.Features.Geo
        config?.cameraResolution = CameraSettings.CameraResolution.AUTO
        config?.cameraFocusMode = CameraSettings.CameraFocusMode.CONTINUOUS
        config?.isCamera2Enabled = true
        // The camera2 api is disabled by default (old camera api is used).

        try {
            architectView.onCreate(config)
        } catch (e: Exception) {
            Log.e("architectView", "architectView.onCreate(config)")
            e.printStackTrace()
            architectView.onCreate(config)
        }

        try {
            if (wikiType === ARGEOCONST.EXTRA_AR_TYPE) {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_URL
            } else {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_GEO_URL
            }

            //architectView.load(currentWorld)
        } catch (e: Exception) {
            Log.e("architectView", "architectView.load")
            e.printStackTrace()
        }

    }

    override fun onLowMemory() {
        super.onLowMemory()

        try {
            architectView.onLowMemory()
        } catch (e: Exception) {
            e.printStackTrace()
            //architectView.onLowMemory()
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        try {
            architectView.onPostCreate()
        } catch (e: Exception) {
            Log.e("architectView", "architectView.onPostCreate()")
            e.printStackTrace()
            architectView.onPostCreate()
        }

        try {
            /*
             * Loads the AR-Experience, it may be a relative path from assets,
             * an absolute path (file://) or a server url.
             *
             * To get notified once the AR-Experience is fully loaded,
             * an ArchitectWorldLoadedListener can be registered.
             */

            /*if (wikiType == ARGEOCONST.EXTRA_AR_TYPE) {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_URL;
            } else {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_GEO_URL;
            }*/

            architectView.load(currentWorld)//SAMPLES_ROOT + arExperience);//
            architectView.registerWorldLoadedListener(object : ArchitectView.ArchitectWorldLoadedListener {
                override fun worldWasLoaded(p0: String?) {
                    Toast.makeText(this@SimpleArActivity, "success", Toast.LENGTH_LONG).show()
                }

                override fun worldLoadFailed(p0: Int, p1: String?, p2: String?) {
                    Toast.makeText(this@SimpleArActivity, "failed! $p0! ${p1}! ${p2}", Toast.LENGTH_LONG).show()
                    architectView.load(currentWorld)
                }
            })
        } catch (e: Exception) {
            //Toast.makeText(this, getString(R.string.error_loading_ar_experience), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience $arExperience.", e)
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            architectView.onResume(); // Mandatory ArchitectView lifecycle call
        } catch (e: Exception) {
            Log.e("architectView", "architectView.onResume()")
            e.printStackTrace()

            architectView.onResume();
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            architectView.onPause(); // Mandatory ArchitectView lifecycle call
        } catch (e: Exception) {
            Log.e("architectView", "architectView.onPause()")
            e.printStackTrace()

            architectView.onPause();
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        /*
         * Deletes all cached files of this instance of the ArchitectView.
         * This guarantees that internal storage for this instance of the ArchitectView
         * is cleaned and app-memory does not grow each session.
         *
         * This should be called before architectView.onDestroy
         */
        try {
            architectView.clearCache()
            architectView.onDestroy() // Mandatory ArchitectView lifecycle call
        } catch (e: Exception) {
            Log.e("architectView", "architectView.clearCache()")
            e.printStackTrace()

            architectView.onDestroy()
        }

    }

    companion object {

        val INTENT_EXTRAS_KEY_SAMPLE = "sampleData"

        val ACTIVITY_ARCHITECT_WORLD_URL = "https://hb.bizmrg.com/image-target/experience/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/experience/index.html";
        val ACTIVITY_ARCHITECT_WORLD_GEO_URL = "https://hb.bizmrg.com/geo-target/index.html"//"https://storage.cloud.croc.ru/zrenie.kudinov/geo/index.html";

        var currentWorld = ACTIVITY_ARCHITECT_WORLD_URL

        private val TAG = SimpleArActivity::class.java.simpleName

        private val EXTRA_TYPE = "EXTRA_TYPE"

        /** Root directory of the sample AR-Experiences in the assets dir.  */
        private val SAMPLES_ROOT = "samples/"
    }
}
