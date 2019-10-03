package com.zrenie20don;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectStartupConfiguration.Features;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

//import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
//import com.wikitude.architect.services.camera.CameraLifecycleListener;

/**
 * Abstract activity which handles live-cycle events.
 * Feel free to extend from this activity when setting up your own AR-Activity
 *
 */
public abstract class AbstractArchitectCamActivity extends Activity implements ArchitectViewHolderInterface, View.OnTouchListener {

	/**
	 * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
	 */
	protected ArchitectView architectView;

	/**
	 * sensor accuracy listener in case you want to display calibration hints
	 */
	protected SensorAccuracyChangeListener sensorAccuracyListener;

	/**
	 * last known location of the user, used internally for content-loading after user location was fetched
	 */
	protected Location lastKnownLocaton;

	/**
	 * sample location strategy, you may implement a more sophisticated approach too
	 */
	protected ILocationProvider				locationProvider;

	/**
	 * location listener receives location updates and must forward them to the architectView
	 */
	protected LocationListener locationListener;

	/**
	 * urlListener handling "document.location= 'architectsdk://...' " calls in JavaScript"
	 */
	//protected ArchitectUrlListener urlListener;

	protected JSONArray poiData;
	ArchitectStartupConfiguration config;
	protected boolean isLoading = false;

	private boolean flash;
	private float zoom_max = 0;
//	Camera camera;
	Camera.Parameters p ;

	SurfaceView sv;
	SurfaceHolder holder;
	//HolderCallback holderCallback;
	Camera camera;
	SeekBar seekBarZoom;
	Surface surfaceView;
	//private WikitudeSDK _wikitudeSDK;

	private CameraManager mCameraManager;  // камера Для версии выше api-22
	private String mCameraId;  // камера Для версии выше api-22
	Camera.Parameters parameters; // камера Для версии ниже или равно api-22
	protected CameraCaptureSession captureSession;

	private final String[] permissions = new String[] {
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	/** Called when the activity is first created. */
	@SuppressLint({"NewApi", "ResourceType", "ClickableViewAccessibility"})
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		try {
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);


			this.setContentView(R.layout.architect_cam);

			this.setTitle(this.getActivityTitle());

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
					WebView.setWebContentsDebuggingEnabled(true);
				}
			}

			this.architectView = (ArchitectView) this.findViewById(R.id.architectView);

			findViewById(R.id.flash_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
						flashLihgt(flash =! flash);
				}
			});



			if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP_MR1){
                findViewById(R.id.foto_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            architectView.captureScreen(1, new ArchitectView.CaptureScreenCallback() {
                                @Override
                                public void onScreenCaptured(Bitmap bitmap) {
                                    try {
                                        saveImage(bitmap);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (Exception e) {
							findViewById(R.id.foto_button).setVisibility(View.INVISIBLE);
                           // Toast.makeText(AbstractArchitectCamActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                findViewById(R.id.foto_button).setVisibility(View.INVISIBLE);
            }
			try {
				architectView.captureScreen(1, new ArchitectView.CaptureScreenCallback() {
					@Override
					public void onScreenCaptured(Bitmap bitmap) {
						try {
							saveImage(bitmap);
							findViewById(R.id.foto_button).setVisibility(View.VISIBLE);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			} catch (Exception e) {
				findViewById(R.id.foto_button).setVisibility(View.INVISIBLE);
				// Toast.makeText(AbstractArchitectCamActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//architectView.z
					Intent browserIntent = new
							Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zrenie20.info"));
					startActivity(browserIntent);
				}
			});

			try {

				config = new ArchitectStartupConfiguration();
				config.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY );
				config.setFeatures(Features.Geo);
				config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
				config.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
				config.setCamera2Enabled(true);
				this.architectView.onCreate( config );
				//architectView.onFl
			} catch (RuntimeException rex) {
				this.architectView = null;
				//Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
				Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
			} catch (Exception e) {

			}


			//this.sensorAccuracyListener = this.getSensorAccuracyListener();


			if (hasGeo()) {
				// listener passed over to locationProvider, any location update is handled here
				this.locationListener = new LocationListener() {

					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
					}

					@Override
					public void onProviderEnabled(String provider) {
					}

					@Override
					public void onProviderDisabled(String provider) {
					}

					@Override
					public void onLocationChanged(final Location location) {
						// forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
						if (location != null) {
							// sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
							AbstractArchitectCamActivity.this.lastKnownLocaton = location;
							if (AbstractArchitectCamActivity.this.architectView != null) {
								// check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
								if (location.hasAltitude() && location.hasAccuracy() && location.getAccuracy() < 7) {
									AbstractArchitectCamActivity.this.architectView.setLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
								} else {
									AbstractArchitectCamActivity.this.architectView.setLocation(location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000);
								}
							}
						}
					}
				};

				// locationProvider used to fetch user position
				this.locationProvider = getLocationProvider(this.locationListener);
			} else {
				this.locationProvider = null;
				this.locationListener = null;
			}



		}catch (Exception e){
			startActivity(new Intent(this, MainActivity.class));
		}
	}

	protected boolean getCamera2Enabled() {
		return false;
	}
	protected abstract CameraSettings.CameraPosition getCameraPosition();

	private void saveImage(Bitmap finalBitmap) {

		String root = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		File myDir = new File(root + "/saved_images");
		myDir.mkdirs();
		Random generator = new Random();

		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-"+ n +".jpg";
		File file = new File (myDir, fname);
		if (file.exists ()) file.delete ();
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
			//     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
// Tell the media scanner about the new file so that it is
// immediately available to the user.
		MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						Log.i("ExternalStorage", "Scanned " + path + ":");
						Log.i("ExternalStorage", "-> uri=" + uri);
					}
				});
	}

	private int getFeatures() {
		int features = (hasGeo() ? ArchitectStartupConfiguration.Features.Geo : 0) | (hasIR() ? Features.ImageTracking : 0);
		return features;
	}

	protected abstract boolean hasGeo();
	protected abstract boolean hasIR();

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public void flashLihgt(boolean on){
		try {
		//	architectView.(0.2f);
			CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

			try {
				String cameraId = cameraManager.getCameraIdList()[0];
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					cameraManager.setTorchMode(cameraId, true);
				}
				//flashLightStatus = true;
				//imageFlashlight.setImageResource(R.drawable.btn_switch_on);
			} catch (CameraAccessException e) {
			}
		//	mCam.stopPreview();
		//	startActivity(new Intent(this, ArchitectCamActivity.class));
		//	finish();
//			try {
//
//				config = new ArchitectStartupConfiguration();
//				config.setLicenseKey(WikitudeSDKConstants.WIKITUDE_TRIAL_KEY );
//				config.setFeatures(Features.Geo);
//				config.setCameraResolution(CameraSettings.CameraResolution.FULL_HD_1920x1080);
//				config.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
//				config.setCamera2Enabled(true);
//				this.architectView.onCreate( config );
//
//			} catch (RuntimeException rex) {
//				this.architectView = null;
//				Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
//				Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
//			} catch (Exception e) {
//
//			}
		} catch (Exception e2) {
		}


	}
	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );

		if ( this.architectView != null ) {

			// call mandatory live-cycle method of architectView
			try {
				try {
					this.architectView.onPostCreate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
				this.architectView.load( this.getARchitectWorldPath() );
				//this.architectView.load("http://goo.gl/FVf2dI");
				if (this.getInitialCullingDistanceMeters() != ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS) {
					// set the culling distance - meaning: the maximum distance to render geo-content
					this.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

				// load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
			//	this.architectView.load( this.getARchitectWorldPath() );

		}
	}

	private void getOlDCamera(){
		architectView.setVisibility(View.GONE);
		Camera camera = Camera.open(0);
		Camera.Parameters parameters = camera.getParameters();
		parameters.setZoom(parameters.getMaxZoom()/2);
		camera.setParameters(parameters);
		camera.release();
	}

	@SuppressLint("MissingPermission")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void getCamera() {
		final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				manager.openCamera("0", new CameraDevice.StateCallback() {
					@Override
					public void onOpened(@NonNull CameraDevice cameraDevice) {
						try {
							cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
							float maxZoom = (manager.getCameraCharacteristics("0").get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;

							Rect m = manager.getCameraCharacteristics("0").get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
							//zoom_level = 8;

							int minW = (int) (m.width() / maxZoom);
							int minH = (int) (m.height() / maxZoom);
							int difW = m.width() - minW;
							int difH = m.height() - minH;
							int cropW = difW / 100 * (int) zoom_level;
							int cropH = difH / 100 * (int) zoom_level;
							cropW -= cropW & 3;
							cropH -= cropH & 3;
//							Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
//							mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
//
							final CaptureRequest.Builder captureBuilder =
									cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

							Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
							captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
							//	captureBuilder.set();
							CameraCaptureSession.CaptureCallback CaptureCallback
									= new CameraCaptureSession.CaptureCallback() {

								@RequiresApi(api = Build.VERSION_CODES.M)
								@Override
								public void onCaptureStarted(@NonNull CameraCaptureSession session,
															 @NonNull CaptureRequest request,
															 @NonNull long timestamp,
															 @NonNull long framenumber) {
									//playShutterSound();
									//showShutterAnimation();
									mCaptureSession = session;
									try {
										mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), this,
												null);
									} catch (CameraAccessException e) {
										e.printStackTrace();
									} catch (NullPointerException ex) {
										ex.printStackTrace();
									}
									session.getInputSurface();

								}

								@Override
								public void onCaptureCompleted(@NonNull CameraCaptureSession session,
															   @NonNull CaptureRequest request,
															   @NonNull TotalCaptureResult result) {
									mCaptureSession = session;
									try {
										mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), this,
												null);
									} catch (CameraAccessException e) {
										e.printStackTrace();
									} catch (NullPointerException ex) {
										ex.printStackTrace();
									}
									//session.getInputSurface();

								}
							};
							//manager.registerAvailabilityCallback(CaptureCallback, new Handler());

							//	mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
						} catch (CameraAccessException e) {
							e.printStackTrace();
						}
						//	cameraDevice.createCaptureSessionByOutputConfigurations();
						//CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback();
					}

					@Override
					public void onDisconnected(@NonNull CameraDevice cameraDevice) {

					}

					@Override
					public void onError(@NonNull CameraDevice cameraDevice, int i) {

					}
				}, new Handler());
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (zoom_level < 80) {
						zoom_level++;
						getCamera();
					}
				}
			}, 400);
		}

	}

	@Override
	public void onBackPressed() {
		finish();
	}


	private void zoomOut(){
		try{
			zoom_max -= 1;
			if ( zoom_max > 0) {
				architectView.setCameraDistance(zoom_max);
			} else {
				architectView.setCameraDistance(0);
				zoom_max = 0;
			}
		}catch (Exception e){

		}

	}


	@Override
	protected void onResume() {
		super.onResume();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.onResume();

			// register accuracy listener in architectView, if set
			if (this.sensorAccuracyListener!=null) {
				this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}

		// tell locationProvider to resume, usually location is then (again) fetched, so the GPS indicator appears in status bar
		if ( this.locationProvider != null ) {
			this.locationProvider.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.onPause();

			// unregister accuracy listener in architectView, if set
			if ( this.sensorAccuracyListener != null ) {
				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}

		// tell locationProvider to pause, usually location is then no longer fetched, so the GPS indicator disappears in status bar
		if ( this.locationProvider != null ) {
			this.locationProvider.onPause();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( this.architectView != null ) {
			this.architectView.onLowMemory();
		}
	}

	/**
	 * title shown in activity
	 * @return
	 */
	public abstract String getActivityTitle();

	/**
	 * path to the architect-file (AR-Experience HTML) to launch
	 * @return
	 */
	@Override
	public abstract String getARchitectWorldPath();

	/**
	 * url listener fired once e.g. 'document.location = "architectsdk://foo?bar=123"' is called in JS
	 * @return
	 */
	//@Override
	//public abstract ArchitectUrlListener getUrlListener();

	/**
	 * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
	 */
	@Override
	public abstract int getContentViewId();

	/**
	 * @return Wikitude SDK license key, checkout www.wikitude.com for details
	 */
	@Override
	public abstract String getWikitudeSDKLicenseKey();

	/**
	 * @return layout-id of architectView, e.g. R.id.architectView
	 */
	@Override
	public abstract int getArchitectViewId();

	/**
	 *
	 * @return Implementation of a Location
	 */
	@Override
	public abstract ILocationProvider getLocationProvider(final LocationListener locationListener);

	/**
	 * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
	 */
	//@Override
	//public abstract ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener();

	/**
	 * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
	 * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
	 */
	public static final boolean isVideoDrawablesSupported() {
		String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		return extensions != null && extensions.contains( "GL_OES_EGL_image_external" ) && android.os.Build.VERSION.SDK_INT >= 14 ;
	}

	protected void injectData() {
		if (!isLoading) {
			final Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					isLoading = true;

					final int WAIT_FOR_LOCATION_STEP_MS = 2000;

					while (lastKnownLocaton==null && !isFinishing()) {

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
							//	Toast.makeText(AbstractArchitectCamActivity.this, R.string.location_fetching, Toast.LENGTH_SHORT).show();
							}
						});

						try {
							Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
						} catch (InterruptedException e) {
							break;
						}
					}

					if (lastKnownLocaton!=null && !isFinishing()) {
						// TODO: you may replace this dummy implementation and instead load POI information e.g. from your database
						poiData = getPoiInformation(lastKnownLocaton, 20);
						callJavaScript("World.loadPoisFromJsonData", new String[] { poiData.toString() });
					}

					isLoading = false;
				}
			});
			t.start();
		}
	}

	/**
	 * call JacaScript in architectView
	 * @param methodName
	 * @param arguments
	 */
	private void callJavaScript(final String methodName, final String[] arguments) {
		final StringBuilder argumentsString = new StringBuilder("");
		for (int i= 0; i<arguments.length; i++) {
			argumentsString.append(arguments[i]);
			if (i<arguments.length-1) {
				argumentsString.append(", ");
			}
		}

		if (this.architectView!=null) {
			final String js = ( methodName + "( " + argumentsString.toString() + " );" );
			this.architectView.callJavascript(js);
		}
	}

	/**
	 * loads poiInformation and returns them as JSONArray. Ensure attributeNames of JSON POIs are well known in JavaScript, so you can parse them easily
	 * @param userLocation the location of the user
	 * @param numberOfPlaces number of places to load (at max)
	 * @return POI information in JSONArray
	 */
	public static JSONArray getPoiInformation(final Location userLocation, final int numberOfPlaces) {

		if (userLocation==null) {
			return null;
		}

		final JSONArray pois = new JSONArray();

		// ensure these attributes are also used in JavaScript when extracting POI data
		final String ATTR_ID = "id";
		final String ATTR_NAME = "name";
		final String ATTR_DESCRIPTION = "description";
		final String ATTR_LATITUDE = "latitude";
		final String ATTR_LONGITUDE = "longitude";
		final String ATTR_ALTITUDE = "altitude";

		for (int i=1;i <= numberOfPlaces; i++) {
			final HashMap<String, String> poiInformation = new HashMap<String, String>();
			poiInformation.put(ATTR_ID, String.valueOf(i));
			poiInformation.put(ATTR_NAME, "POI#" + i);
			poiInformation.put(ATTR_DESCRIPTION, "This is the description of POI#" + i);
			double[] poiLocationLatLon = getRandomLatLonNearby(userLocation.getLatitude(), userLocation.getLongitude());
			poiInformation.put(ATTR_LATITUDE, String.valueOf(poiLocationLatLon[0]));
			poiInformation.put(ATTR_LONGITUDE, String.valueOf(poiLocationLatLon[1]));
			final float UNKNOWN_ALTITUDE = -32768f;  // equals "AR.CONST.UNKNOWN_ALTITUDE" in JavaScript (compare AR.GeoLocation specification)
			// Use "AR.CONST.UNKNOWN_ALTITUDE" to tell ARchitect that altitude of places should be on user level. Be aware to handle altitude properly in locationManager in case you use valid POI altitude value (e.g. pass altitude only if GPS accuracy is <7m).
			poiInformation.put(ATTR_ALTITUDE, String.valueOf(UNKNOWN_ALTITUDE));
			pois.put(new JSONObject(poiInformation));
		}

		return pois;
	}

	/**
	 * helper for creation of dummy places.
	 * @param lat center latitude
	 * @param lon center longitude
	 * @return lat/lon values in given position's vicinity
	 */
	private static double[] getRandomLatLonNearby(final double lat, final double lon) {
		return new double[] { lat + Math.random()/5-0.1 , lon + Math.random()/5-0.1};
	}

	private float getFingerSpacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	protected float fingerSpacing = 0;
	protected float zoomLevel = 1f;


	/***********************Zoom variables***************************/
	public float finger_spacing = 0;
	public int zoom_level = 1;
	/*********************************************************************/
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {


		return true;
	}
	private CameraCaptureSession mCaptureSession;
	private CameraDevice mCameraDevice;
	private Size mPreviewSize;
	private CaptureRequest mPreviewRequest;
	private CaptureRequest.Builder mPreviewRequestBuilder;
}