package com.zrenie20don;

import android.location.LocationListener;
import android.net.Uri;
import android.util.Log;

//import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
//import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.common.camera.CameraSettings;

public class ArchitectCamActivity extends AbstractArchitectCamActivity {


	public static final String ACTIVITY_TITLE_STRING = "activityTitle";
	//public static final String ACTIVITY_ARCHITECT_WORLD_URL = "http://goo.gl/FVf2dI";

	public static final String ACTIVITY_ARCHITECT_WORLD_URL = "https://storage.cloud.croc.ru/zrenie.kudinov/experience/index.html";

	/**
	 * last time the calibration toast was shown, this avoids too many toast shown when compass needs calibration
	 */
	private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

	@Override
	public String getARchitectWorldPath() {
		return ACTIVITY_ARCHITECT_WORLD_URL;
	}

	@Override
	public String getActivityTitle() {
		return ACTIVITY_TITLE_STRING;
	}

	@Override
	public int getContentViewId() {
		return R.layout.architect_cam;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public int getArchitectViewId() {
		return R.id.architectView;
	}

	@Override
	public String getWikitudeSDKLicenseKey() {
		return WikitudeSDKConstants.WIKITUDE_TRIAL_KEY;
	}

	//@Override
	//public SensorAccuracyChangeListener getSensorAccuracyListener() {
	//	return null;
	//}

	@Override
	public ILocationProvider getLocationProvider(final LocationListener locationListener) {
		return null;
	}

	@Override
	public float getInitialCullingDistanceMeters() {
		// you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
		return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
	}

	@Override
	protected boolean hasGeo() {
		return false;
	}

	@Override
	protected boolean hasIR() {
		return false;
	}

	@Override
	protected CameraSettings.CameraPosition getCameraPosition() {
		return CameraSettings.CameraPosition.DEFAULT;
	}
}
