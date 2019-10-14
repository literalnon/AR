package com.zrenie20don;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.camera.CameraSettings;

import java.io.IOException;

import static com.zrenie20don.ZrenieApp.wikiType;

/**
 * This Activity is (almost) the least amount of code required to use the
 * basic functionality for Image-/Instant- and Object Tracking.
 *
 * This Activity needs Manifest.permission.CAMERA permissions because the
 * ArchitectView will try to start the camera.
 */
public class SimpleArActivity extends AppCompatActivity {

    public static final String INTENT_EXTRAS_KEY_SAMPLE = "sampleData";

    public static final String ACTIVITY_ARCHITECT_WORLD_URL = "https://storage.cloud.croc.ru/zrenie.kudinov/experience/index.html";
    public static final String ACTIVITY_ARCHITECT_WORLD_GEO_URL = "https://storage.cloud.croc.ru/zrenie.kudinov/geo/index.html";

    public static String currentWorld = ACTIVITY_ARCHITECT_WORLD_URL;

    private static final String TAG = SimpleArActivity.class.getSimpleName();

    private static final String EXTRA_TYPE = "EXTRA_TYPE";

    /** Root directory of the sample AR-Experiences in the assets dir. */
    private static final String SAMPLES_ROOT = "samples/";

    /**
     * The ArchitectView is the core of the AR functionality, it is the main
     * interface to the Wikitude SDK.
     * The ArchitectView has its own lifecycle which is very similar to the
     * Activity lifecycle.
     * To ensure that the ArchitectView is functioning properly the following
     * methods have to be called:
     *      - onCreate(ArchitectStartupConfiguration)
     *      - onPostCreate()
     *      - onResume()
     *      - onPause()
     *      - onDestroy()
     * Those methods are preferably called in the corresponding Activity lifecycle callbacks.
     */
    protected ArchitectView architectView;
    protected ArchitectStartupConfiguration config;

    /** The path to the AR-Experience. This is usually the path to its index.html. */
    private String arExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.architect_cam);

        // Used to enabled remote debugging of the ArExperience with google chrome https://developers.google.com/web/tools/chrome-devtools/remote-debugging
        WebView.setWebContentsDebuggingEnabled(true);

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
        config = new ArchitectStartupConfiguration(); // Creates a config with its default values.

        config.setLicenseKey(WikitudeSDKConstants.INSTANCE.getWIKITUDE_SDK_KEY());
        config.setFeatures(ArchitectStartupConfiguration.Features.Geo);
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
        config.setCamera2Enabled(true);
        // The camera2 api is disabled by default (old camera api is used).
        
        architectView = findViewById(R.id.architectView);//new ArchitectView(this);
        try {
            architectView.onCreate(config); // create ArchitectView with configuration
        } catch (Exception e) {
            Log.e("architectView", "architectView.onCreate(config)");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            architectView.onPostCreate();
        } catch (Exception e) {
            Log.e("architectView", "architectView.onPostCreate()");
            e.printStackTrace();
        }

        try {
            /*
             * Loads the AR-Experience, it may be a relative path from assets,
             * an absolute path (file://) or a server url.
             *
             * To get notified once the AR-Experience is fully loaded,
             * an ArchitectWorldLoadedListener can be registered.
             */

            if (wikiType == ARGEOCONST.EXTRA_AR_TYPE) {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_URL;
            } else {
                currentWorld = ACTIVITY_ARCHITECT_WORLD_GEO_URL;
            }

            architectView.load(currentWorld);//SAMPLES_ROOT + arExperience);//
        } catch (Exception e) {
            //Toast.makeText(this, getString(R.string.error_loading_ar_experience), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience " + arExperience + ".", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            architectView.onResume(); // Mandatory ArchitectView lifecycle call
        } catch (Exception e) {
            Log.e("architectView", "architectView.onResume()");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            architectView.onPause(); // Mandatory ArchitectView lifecycle call
        } catch (Exception e) {
            Log.e("architectView", "architectView.onPause()");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Deletes all cached files of this instance of the ArchitectView.
         * This guarantees that internal storage for this instance of the ArchitectView
         * is cleaned and app-memory does not grow each session.
         *
         * This should be called before architectView.onDestroy
         */
        try {
            architectView.clearCache();
            architectView.onDestroy(); // Mandatory ArchitectView lifecycle call
        } catch (Exception e) {
            Log.e("architectView", "architectView.clearCache()");
            e.printStackTrace();
        }
    }
}
