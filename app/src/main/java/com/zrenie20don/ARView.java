//package com.zrenie20don;
//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.AlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.ConfigurationInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.hardware.SensorManager;
//import android.location.LocationManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//import android.support.annotation.Nullable;
//import android.text.Html;
//import android.text.method.LinkMovementMethod;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
//import com.wikitude.architect.ArchitectStartupConfiguration;
//import com.wikitude.architect.ArchitectView;
//import com.wikitude.architect.ArchitectWebView;
//import com.wikitude.architect.BrowserActivity;
//import com.wikitude.architect.CallbackHandler;
//import com.wikitude.architect.GameplayInterface;
//import com.wikitude.architect.HtmlDrawableInterface;
//import com.wikitude.architect.HtmlRenderManager;
//import com.wikitude.architect.IArchitectCallbackListener;
//import com.wikitude.architect.PlatformBridge;
//import com.wikitude.architect.j;
//import com.wikitude.architect.l;
//import com.wikitude.architect.services.camera.CameraLifecycleListener;
//import com.wikitude.architect.services.location.internal.LocationService;
//import com.wikitude.architect.util.internal.ArchitectSDKBuildInformationInternal;
//import com.wikitude.common.camera.CameraSettings;
//import com.wikitude.common.camera.internal.CameraService;
//import com.wikitude.common.camera.internal.b;
//import com.wikitude.common.debug.internal.DAssert;
//import com.wikitude.common.files.internal.a;
//import com.wikitude.common.jni.internal.NativeBinding;
//import com.wikitude.common.permission.PermissionManager;
//import com.wikitude.common.plugins.Plugin;
//import com.wikitude.common.plugins.PluginManager;
//import com.wikitude.common.plugins.internal.PluginManagerInternal;
//import com.wikitude.common.rendering.RenderExtension;
//import com.wikitude.common.rendering.RenderSurfaceView;
//import com.wikitude.common.rendering.internal.NativeRenderer;
//import com.wikitude.common.services.internal.ServiceManagerInternal;
//import com.wikitude.common.services.sensors.internal.SensorService;
//import com.wikitude.common.services.sensors.internal.f;
//import com.wikitude.common.util.SDKBuildInformation;
//import com.wikitude.tools.device.features.MissingDeviceFeatures;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Properties;
//import java.util.Scanner;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.regex.Pattern;
//
///**
// * Created by Кирилл on 04.03.2018.
// */
//
//public class ARView  extends ArchitectView implements com.wikitude.common.camera.internal.b, NativeBinding, com.wikitude.common.services.sensors.internal.f {
//    private static SDKBuildInformation j;
//    private static PermissionManager k;
//    private static final String l;
//    private static final String m = "core";
//    private static final String n = "https://sdktracking.wikitude.com";
//    ARView.ViewState b;
//    Activity c;
//    static boolean d;
//    private ServiceManagerInternal o;
//    private String p;
//    private boolean q;
//    private AssetManager r;
//    private com.wikitude.common.files.internal.a s;
//    private static final String t = "libarchitect.so";
//    private long nativePtr;
//    private ARView.ArchitectInitializeException u;
//    private static final Properties v;
//
//    private NativeRenderer z;
//    protected com.wikitude.common.rendering.internal.b f;
//    RenderSurfaceView g;
//    private com.wikitude.common.rendering.internal.a A;
//    private com.wikitude.common.orientation.internal.a B;
//    private com.wikitude.architect.services.sensors.internal.a D;
//    private CameraLifecycleListener F;
//    private Set<ArchitectJavaScriptInterfaceListener> G;
//
//    private ArchitectStartupConfiguration J;
//    private ARView.NetworkStateReceiver K;
//    private PluginManagerInternal L;
//    private List<Plugin> M;
//    private String N;
//    String h;
//    TreeSet<Integer> i;
//    @com.wikitude.common.annotations.internal.b
//    private static final int UNKNOWN_ALTITUDE = -32768;
//    private double O;
//    private double P;
//    private double Q;
//    private double R;
//
//    public ARView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        this.b = ARView.ViewState.undefined;
//        this.p = "JAVASCRIPT_API";
//        this.q = false;
//        this.u = null;
//        this.G = new HashSet();
//        this.M = new ArrayList();
//        this.h = "";
//        this.i = new TreeSet();
//        this.Q = -32768.0D;
//        this.a(context);
//    }
//
//    public ARView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public ARView(Context context) {
//        super(context);
//        this.b = ARView.ViewState.undefined;
//        this.p = "JAVASCRIPT_API";
//        this.q = false;
//        this.u = null;
//        this.G = new HashSet();
//        this.M = new ArrayList();
//        this.h = "";
//        this.i = new TreeSet();
//        this.Q = -32768.0D;
//        this.a(context);
//    }
//
//    public void onCameraOpen() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(var1 != null) {
//            this.setCameraMirroring(var1.e().h() == CameraSettings.CameraPosition.FRONT);
//        }
//
//        this.B.a(var1);
//        if(this.F != null) {
//            this.F.onCameraOpen();
//        }
//
//    }
//
//    public void onCameraReleased() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(var1 != null) {
//            this.B.b(var1);
//        } else {
//            Log.w(l, "onCameraReleased: CameraService is not initialized.");
//        }
//
//        if(this.F != null) {
//            this.F.onCameraReleased();
//        }
//
//    }
//
//    public void onCameraOpenAbort() {
//        if(this.F != null) {
//            this.F.onCameraOpenAbort();
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setCameraLifecycleListener(CameraLifecycleListener cameraLifecycleListener) {
//        this.F = cameraLifecycleListener;
//    }
//
//    public void onSensorServiceStarted() {
//        SensorService var1 = (SensorService)this.o.getService("device_motion");
//        if(null != var1) {
//            var1.a(this.D);
//        } else {
//            Log.e(l, "onSensorServiceStarted: SensorService is not initialized.");
//        }
//
//    }
//
//    public void onSensorServiceStopped() {
//        SensorService var1 = (SensorService)this.o.getService("device_motion");
//        if(null != var1) {
//            var1.b(this.D);
//        } else {
//            Log.e(l, "onSensorServiceStopped: SensorService is not initialized.");
//        }
//
//    }
//
//    private static boolean e() {
//        boolean var0 = false;
//        String var1 = System.getProperty("os.arch");
//        String var2 = var1.substring(0, 3).toUpperCase();
//        if(!var2.equals("X86") && !var2.equals("I68") && !var2.equals("AAR")) {
//            Scanner var3 = null;
//
//            try {
//                for(var3 = new Scanner(new FileInputStream("/proc/cpuinfo")); var3.hasNextLine(); var3.nextLine()) {
//                    if(!var0 && var3.findInLine("neon") != null) {
//                        var0 = true;
//                    }
//                }
//            } catch (Exception var8) {
//                ;
//            } finally {
//                if(var3 != null) {
//                    var3.close();
//                }
//
//            }
//        } else {
//            var0 = true;
//        }
//
//        return var0;
//    }
//
//    private void a(Context var1) {
//        this.c = (Activity)this.getContext();
//        this.s = new a(this.c);
//
//
//        this.setBackgroundColor(-16777216);
//        this.removeAllViews();
//        this.createNative();
//    }
//
//    private static int getNumberOfCores() {
//        return Build.VERSION.SDK_INT >= 17?Runtime.getRuntime().availableProcessors():getNumberOfCoresOldDevices();
//    }
//
//    private static int getNumberOfCoresOldDevices() {
//        try {
//            File[] var0 = (new File("/sys/devices/system/cpu/")).listFiles(new FileFilter() {
//                public boolean accept(File pathname) {
//                    return Pattern.matches("cpu[0-9]+", pathname.getName());
//                }
//            });
//            return var0.length;
//        } catch (Exception var1) {
//            return 1;
//        }
//    }
//
//    private void a(View var1) {
//        Object var2 = var1.getLayoutParams();
//        if(var2 == null) {
//            var2 = this.generateDefaultLayoutParams();
//            if(var2 == null) {
//                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
//            }
//        }
//
//        super.addView(var1, -1, (android.view.ViewGroup.LayoutParams)var2);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setLocation(double lat, double lon, double alt, float accuracy) {
//        LocationService var8 = (LocationService)this.o.getService("location");
//        if(null != var8) {
//            var8.e().setLocation((float)lat, (float)lon, (float)alt, accuracy);
//        } else {
//            this.O = lat;
//            this.P = lon;
//            this.Q = alt;
//            this.R = (double)accuracy;
//            Log.i(l, "setLocation: LocationService is not initialized.");
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setLocation(double lat, double lon, double accuracy) {
//        LocationService var7 = (LocationService)this.o.getService("location");
//        if(null != var7) {
//            var7.e().setLocation((float)lat, (float)lon, -32768.0F, (float)accuracy);
//        } else {
//            this.O = lat;
//            this.P = lon;
//            this.Q = -32768.0D;
//            this.R = accuracy;
//            Log.i(l, "setLocation: LocationService is not initialized.");
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public void registerUrlListener(ArchitectView.ArchitectUrlListener listener) {
//        if(this.a != null) {
//            this.a.registerUrlHandler(listener);
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void registerWorldLoadedListener(ArchitectView.ArchitectWorldLoadedListener listener) {
//        if(this.a != null) {
//            this.a.registerWorldLoadedHandler(listener);
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public void onCreate(String key) {
//        ArchitectStartupConfiguration var2 = new ArchitectStartupConfiguration();
//        var2.setLicenseKey(key);
//        this.onCreate(var2);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void onCreate(ArchitectStartupConfiguration aConfig) {
//        this.J = aConfig;
//        this.p = aConfig.getOrigin();
//        if(this.b == ArchitectView.ViewState.initFailed) {
//            Log.e(l, "delayed exception from constructor", this.u);
//            throw this.u;
//        } else {
//            if(aConfig != null && aConfig.getLicenseKey() != null) {
//                this.N = aConfig.getLicenseKey();
//                this.b = ArchitectView.ViewState.constructed;
//            } else {
//                Log.e(l, "App key not set. Switch to failsave state");
//                this.removeAllViews();
//            }
//
//        }
//    }
//
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        int var2 = event.getPointerId(event.getActionIndex());
//        int var3 = this.g.getWidth();
//        int var4 = this.g.getHeight();
//        int var5 = event.findPointerIndex(var2);
//        if(var5 < 0) {
//            return false;
//        } else {
//            float var6 = event.getX(var5) / (float)var3;
//            float var7 = event.getY(var5) / (float)var4;
//            switch(event.getActionMasked()) {
//                case 0:
//                case 5:
//                    this.i.add(Integer.valueOf(var2));
//                    this.forwardTouchBeganEvent(var2, var6, var7);
//                    break;
//                case 1:
//                case 6:
//                    this.i.remove(Integer.valueOf(var2));
//                    this.forwardTouchEndedEvent(var2, var6, var7);
//                    break;
//                case 2:
//                    int[] var8 = new int[this.i.size()];
//                    float[] var9 = new float[this.i.size()];
//                    float[] var10 = new float[this.i.size()];
//                    int var11 = 0;
//
//                    for(Iterator var12 = this.i.iterator(); var12.hasNext(); ++var11) {
//                        Integer var13 = (Integer)var12.next();
//                        var8[var11] = var13.intValue();
//                        if(event.findPointerIndex(var13.intValue()) < 0) {
//                            return false;
//                        }
//
//                        var9[var11] = event.getX(event.findPointerIndex(var13.intValue())) / (float)var3;
//                        var10[var11] = event.getY(event.findPointerIndex(var13.intValue())) / (float)var4;
//                    }
//
//                    this.forwardTouchChangedEvent(var8, var9, var10);
//                    break;
//                case 3:
//                    this.forwardTouchCancelledEvent(var2, var6, var7);
//                case 4:
//            }
//
//            return super.dispatchTouchEvent(event);
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public CameraSettings.CameraPosition getCurrentCamera() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().h();
//        } else {
//            Log.e(l, "CameraSettings: CameraService is not initialized.");
//            return null;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setCameraPositionSimple(CameraSettings.CameraPosition cameraPosition) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().a(cameraPosition);
//        } else {
//            Log.e(l, "setCameraPositionSimple: CameraService is not initialized.");
//        }
//
//    }
//
//    private long getTrackingId() {
//        String var1 = Settings.Secure.getString(this.getContext().getContentResolver(), "android_id");
//        if(var1 == null || var1.trim().equals("")) {
//            var1 = Build.BRAND + Build.MODEL;
//        }
//
//        return (long)("wiki" + var1 + "tude").hashCode();
//    }
//
//    private void f() {
//        SharedPreferences var1 = this.getContext().getSharedPreferences("WTTRACKED", 0);
//        var1.edit().putLong("value", this.getTrackingId()).commit();
//    }
//
//    private boolean g() {
//        SharedPreferences var1 = this.getContext().getSharedPreferences("WTTRACKED", 0);
//        return var1.getLong("value", 0L) == this.getTrackingId();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void onPostCreate() {
//        super.onPostCreate();
//
//    }
//
//    private void a(String var1) {
//        if(var1.equalsIgnoreCase("invalid")) {
//            ((TextView)(new AlertDialog.Builder(this.c)).setTitle("License key is missing or invalid").setMessage(Html.fromHtml("<p>Please add a valid license key to your app. <br><br> <a href=\"http://www.wikitude.com/external/doc/documentation/latest/android/triallicense.html#free-trial-license\">More information</a></p>")).show().findViewById(16908299)).setMovementMethod(LinkMovementMethod.getInstance());
//            Log.e(l, "License key is missing or invalid. Please add a valid license key to your app.");
//        }
//
//    }
//
//    private void h() {
//        ConnectivityManager var1 = (ConnectivityManager)this.getContext().getSystemService("connectivity");
//        NetworkInfo var2 = var1.getActiveNetworkInfo();
//        if(var2 != null && var2.isConnectedOrConnecting()) {
//            if(var2.getType() == 1) {
//                this.setNetworkStatus(ARView.NetworkStatus.WIFI.name());
//            } else {
//                this.setNetworkStatus(ARView.NetworkStatus.MOBILE.name());
//            }
//        } else {
//            this.setNetworkStatus(ARView.NetworkStatus.NONE.name());
//        }
//
//        this.K = new ARView.NetworkStateReceiver(this);
//        this.getContext().registerReceiver(this.K, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
//    }
//
//    private void i() {
//        this.e.c(this.nativePtr);
//        this.w.connectNative(this.nativePtr);
//        this.x.connectNative(this.nativePtr);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void onResume() {
//     super.onResume();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void onPause() {
//        super.onPause();
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void onDestroy() {
//        super.onDestroy();
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void clearCache() {
//        this.clearCacheInternal();
//        this.s.b();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static void deleteRootCacheDirectory(Context context) {
//        a.c(context);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void callJavascript(String jsCmd) {
//        this.a.callJavaScript(jsCmd);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void load(String urlString) throws IOException {
//        super.load(urlString);
//    }
//
//
//
//    @com.wikitude.common.annotations.internal.a
//    public void onLowMemory() {
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static String getCacheDirectoryAbsoluteFilePath(Context context) {
//        return a.a(context).getAbsolutePath();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static int getSupportedFeaturesForDevice(Context context) {
//        int var1 = 0;
//        boolean var2;
//        if(Build.VERSION.SDK_INT >= 17) {
//            var2 = context.getPackageManager().hasSystemFeature("android.hardware.camera.any");
//        } else {
//            var2 = context.getPackageManager().hasSystemFeature("android.hardware.camera.front") || context.getPackageManager().hasSystemFeature("android.hardware.camera");
//        }
//
//        if(getOpenGLVersion(context) >= 131072.0F && var2) {
//            if(e()) {
//                if(getNumberOfCores() >= 4) {
//                    var1 |= 2;
//                    var1 |= 8;
//                } else if(getNumberOfCores() >= 2) {
//                    var1 |= 2;
//                }
//            }
//
//            LocationManager var3 = (LocationManager)context.getSystemService("location");
//            SensorManager var4 = (SensorManager)context.getSystemService("sensor");
//            if(var4 != null && var4.getDefaultSensor(1) != null && var4.getDefaultSensor(2) != null) {
//                if(e() && getNumberOfCores() >= 4) {
//                    var1 |= 4;
//                }
//
//                if(var3 != null && var3.getAllProviders() != null && var3.getAllProviders().size() > 0) {
//                    var1 |= 1;
//                }
//            }
//        }
//
//        return var1;
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static boolean isDeviceSupported(Context context) {
//        return (getSupportedFeaturesForDevice(context) & 15) == 15;
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static MissingDeviceFeatures isDeviceSupported(Context context, int features) {
//        String var2 = System.getProperty("line.separator");
//        String var3 = "";
//        boolean var4;
//        if(Build.VERSION.SDK_INT >= 17) {
//            var4 = context.getPackageManager().hasSystemFeature("android.hardware.camera.any");
//        } else {
//            var4 = context.getPackageManager().hasSystemFeature("android.hardware.camera.front") || context.getPackageManager().hasSystemFeature("android.hardware.camera");
//        }
//
//        if(!var4) {
//            var3 = var3 + "- Camera" + var2;
//        }
//
//        if(getOpenGLVersion(context) < 131072.0F) {
//            var3 = var3 + "- OpenGLES version 2.0.+" + var2;
//        }
//
//        SensorManager var5 = (SensorManager)context.getSystemService("sensor");
//        if((features & 1) == 1) {
//            LocationManager var6 = (LocationManager)context.getSystemService("location");
//            if(var6 == null || var6.getAllProviders() == null || var6.getAllProviders().size() <= 0) {
//                var3 = var3 + "- GPS / Location Provider" + var2;
//            }
//        }
//
//        if((features & 2) == 2 || (features & 4) == 4 || (features & 8) == 8) {
//            if(!e()) {
//                var3 = var3 + "- Chipset supporting NEON" + var2;
//            }
//
//            if((features & 2) == 2 && getNumberOfCores() < 2) {
//                var3 = var3 + "- Dual-Core CPU" + var2;
//            }
//
//            if(((features & 4) == 4 || (features & 8) == 8) && getNumberOfCores() < 4) {
//                var3 = var3 + "- Quad-Core CPU" + var2;
//            }
//        }
//
//        if((features & 1) == 1 || (features & 4) == 4) {
//            if(var5 == null || var5.getDefaultSensor(1) == null) {
//                var3 = var3 + "- Accelerometer" + var2;
//            }
//
//            if(var5 == null || var5.getDefaultSensor(2) == null) {
//                var3 = var3 + "- Compass" + var2;
//            }
//        }
//
//        return new MissingDeviceFeatures(!var3.isEmpty(), "The device is missing following features:" + var2 + var3);
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public String getSdkVersion() {
//        return d?this.getArchitectVersion():"N.A.";
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static String getSDKVersion() {
//        return ((ArchitectSDKBuildInformationInternal)j).getSDKVersion();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setCullingDistance(float distance) {
//        if(this.C != null) {
//            this.C.setCullingDistance(distance);
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public float getCullingDistance() {
//        return this.C != null?this.C.getCullingDistance():3.4028235E38F;
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public void clearAppCache() {
//        this.clearCache();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    private static float getOpenGLVersion(Context context) {
//        ActivityManager var1 = (ActivityManager)context.getSystemService("activity");
//        ConfigurationInfo var2 = var1.getDeviceConfigurationInfo();
//        return (float)var2.reqGlEsVersion;
//    }
//
//    private static boolean a(ApplicationInfo var0) {
//        return var0 != null && 2 == (2 & var0.flags);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void registerSensorAccuracyChangeListener(ArchitectView.SensorAccuracyChangeListener sensorAccuracyChangeListener) {
//        this.D.a(sensorAccuracyChangeListener);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void unregisterSensorAccuracyChangeListener(ArchitectView.SensorAccuracyChangeListener sensorAccuracyChangeListener) {
//        this.D.b(sensorAccuracyChangeListener);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void captureScreen(int captureMode, final FileOutputStream fOut) throws IllegalArgumentException {
//        boolean var3 = true;
//        ArchitectView.CaptureScreenCallback var4 = new ArchitectView.CaptureScreenCallback() {
//            public void onScreenCaptured(Bitmap camBitmap) {
//                if(camBitmap != null && ArchitectView.this.getContext() != null) {
//                    camBitmap.compress(Bitmap.CompressFormat.PNG, Math.max(0, Math.min(100, 100)), fOut);
//                }
//
//            }
//        };
//        this.captureScreen(captureMode, var4);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void captureScreen(int captureMode, final ArchitectView.CaptureScreenCallback callback) throws IllegalArgumentException {
//        switch(captureMode) {
//            case 0:
//                this.E.a(callback);
//                break;
//            case 1:
//                ArchitectView.CaptureScreenCallback var3 = new ArchitectView.CaptureScreenCallback() {
//                    public void onScreenCaptured(final Bitmap camBitmap) {
//                        if(ARView.this.a != null) {
//                            ARView.this.c.runOnUiThread(new Runnable() {
//                                public void run() {
//                                    Bitmap var1;
//                                    try {
//                                        var1 = Bitmap.createBitmap(camBitmap.getWidth(), camBitmap.getHeight(), camBitmap.getConfig());
//                                        Canvas var2 = new Canvas(var1);
//                                        var2.drawBitmap(camBitmap, new Matrix(), (Paint)null);
//                                        Bitmap var3 = Bitmap.createBitmap(ARView.this.a.getWidth(), ARView.this.a.getHeight(), Bitmap.Config.ARGB_8888);
//                                        Canvas var4 = new Canvas(var3);
//                                        ARView.this.a.layout(0, 0, ARView.this.a.getWidth(), ARView.this.a.getHeight());
//                                        ARView.this.a.draw(var4);
//                                        var2.drawBitmap(var3, new Matrix(), (Paint)null);
//                                    } catch (Exception var6) {
//                                        Log.w(ArchitectView.l, "Can't capture screen - will return null", var6);
//                                        var1 = null;
//                                    }
//
//                                    try {
//                                        callback.onScreenCaptured(var1);
//                                    } catch (Exception var5) {
//                                        Log.e(ARView.l, "Capture screen callback threw an exception", var5);
//                                    }
//
//                                }
//                            });
//                        }
//
//                    }
//                };
//                this.E.a(var3);
//                break;
//            default:
//                throw new IllegalArgumentException("captureMode must be listed in CaptureScreenCallback.CAPTURE_MODE_*");
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public void setDisplayMode3dInCore(boolean is3d) {
//        this.setHardwareConfiguration("{\"3dmode\":\"" + (is3d?"3d":"2d") + "\"}");
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setFlashEnabled(boolean enabled) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().a(enabled);
//        } else {
//            Log.e(l, "setFlashEnabled: CameraService is not initialized.");
//        }
//
//    }
//
//    public static String getBuildProperty(String name) {
//        return v.getProperty(name);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static SDKBuildInformation getSDKBuildInformation() {
//        return j;
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public static PermissionManager getPermissionManager() {
//        return k;
//    }
//
//    void a(String var1, boolean var2) {
//        if(var1 != null && var1.length() >= 4) {
//            if(var2) {
//                Uri var3;
//                try {
//                    (new URL(var1)).getProtocol();
//                    var3 = Uri.parse(var1);
//                } catch (Exception var9) {
//                    var3 = Uri.parse("http://" + var1);
//                }
//
//                Intent var4 = new Intent("android.intent.action.VIEW");
//                var4.setData(var3);
//                if(var1.startsWith("file")) {
//                    PackageManager var5 = this.c.getPackageManager();
//                    Intent var6 = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
//                    List var7 = var5.queryIntentActivities(var6, 0);
//                    if(!var7.isEmpty()) {
//                        var5.getLaunchIntentForPackage(((ResolveInfo)var7.get(0)).activityInfo.packageName);
//                        ComponentName var8 = new ComponentName(((ResolveInfo)var7.get(0)).activityInfo.packageName, ((ResolveInfo)var7.get(0)).activityInfo.name);
//                        var4.addCategory("android.intent.category.BROWSABLE");
//                        var4.setComponent(var8);
//                        this.getContext().startActivity(var4);
//                    }
//                } else {
//                    this.getContext().startActivity(var4);
//                }
//            } else {
//                Intent var10 = new Intent(this.getContext(), BrowserActivity.class);
//                var10.putExtra("URL", var1);
//                this.getContext().startActivity(var10);
//            }
//
//        } else {
//            Toast.makeText(this.getContext(), "invalid URL provided", 1).show();
//        }
//    }
//
//    boolean c() {
//        return this.a.isActivityFinishing();
//    }
//
//    String getLastLoadedUrl() {
//        return this.a.getLastLoadedUrl();
//    }
//
//    void setCameraZoomLevel(float level) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().a(level);
//        } else {
//            Log.e(l, "setCameraZoomLevel: CameraService is not initialized.");
//        }
//
//    }
//
//    void setCameraFocusMode(CameraSettings.CameraFocusMode mode) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().a(mode);
//        } else {
//            Log.e(l, "setCameraFocusMode: CameraService is not initialized.");
//        }
//
//    }
//
//    void setCameraPosition(CameraSettings.CameraPosition position) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().a(position);
//        } else {
//            Log.e(l, "setCameraPosition: CameraService is not initialized.");
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public CameraSettings.CameraFocusMode getCameraFocusMode() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().k();
//        } else {
//            Log.e(l, "CameraSettings: CameraService is not initialized.");
//            return null;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public float getCameraZoomLevel() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().e();
//        } else {
//            Log.e(l, "getCameraZoomLevel: CameraService is not initialized.");
//            return -1.0F;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public float getCameraMaxZoomLevel() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().f();
//        } else {
//            Log.e(l, "getCameraMaxZoomLevel: CameraService is not initialized.");
//            return -1.0F;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public String[] getAvailableCameraFocusModes() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().l();
//        } else {
//            Log.e(l, "getAvailableCameraFocusModes: CameraService is not initialized.");
//            return null;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public String[] getAvailableCameraPositions() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().g();
//        } else {
//            Log.e(l, "getAvailableCameraPositions: CameraService is not initialized.");
//            return null;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public boolean isCameraManualFocusAvailable() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().i();
//        } else {
//            Log.e(l, "isCameraManualFocusAvailable: CameraService is not initialized.");
//            return false;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void setCameraManualFocusDistance(float cameraFocusDistance) {
//        CameraService var2 = (CameraService)this.o.getService("camera");
//        if(null != var2) {
//            var2.e().b(cameraFocusDistance);
//        } else {
//            Log.e(l, "setCameraManualFocusDistance: CameraService is not initialized.");
//        }
//
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public float getCameraManualFocusDistance() {
//        CameraService var1 = (CameraService)this.o.getService("camera");
//        if(null != var1) {
//            return var1.e().j();
//        } else {
//            Log.e(l, "getCameraManualFocusDistance: CameraService is not initialized.");
//            return -1.0F;
//        }
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public void registerPlugin(Plugin plugin, PluginManager.PluginErrorCallback pluginCallback) {
//        if(this.a(plugin.getIdentifier(), (PluginManager.PluginErrorCallback)null)) {
//            this.L.registerPlugin(plugin, pluginCallback);
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public boolean registerPlugin(Plugin plugin) {
//        return this.a(plugin.getIdentifier(), (PluginManager.PluginErrorCallback)null) && this.L.registerPlugin(plugin);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void registerNativePlugins(String libraryName, PluginManager.PluginErrorCallback pluginCallback) {
//        if(this.a(libraryName, (PluginManager.PluginErrorCallback)null)) {
//            this.L.registerNativePlugins(libraryName, pluginCallback);
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public boolean registerNativePlugins(String libraryName) {
//        return this.registerNativePlugins(libraryName, "");
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void registerNativePlugins(String libraryName, String pluginName, PluginManager.PluginErrorCallback pluginCallback) {
//        if(this.a(pluginName, (PluginManager.PluginErrorCallback)null)) {
//            this.L.registerNativePlugins(libraryName, pluginName, pluginCallback);
//        }
//
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public boolean registerNativePlugins(String libraryName, String pluginName) {
//        return this.a(pluginName, (PluginManager.PluginErrorCallback)null) && this.L.registerNativePlugins(libraryName, pluginName);
//    }
//
//    private boolean a(@Nullable String var1, @Nullable PluginManager.PluginErrorCallback var2) {
//        if(this.b.ordinal() >= 1 && this.b != ArchitectView.ViewState.initFailed) {
//            return true;
//        } else {
//            String var3 = "Registration of plugin '" + var1 + "' was canceled. Wrong lifecycle state detected.";
//            if(var2 == null) {
//                Log.e(l, var3);
//            } else {
//                var2.onRegisterError(4001, var3);
//            }
//
//            return false;
//        }
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    List<Plugin> getRegisteredPlugins() {
//        return this.L.a();
//    }
//
//    private static Properties j() {
//        Properties var0 = new Properties();
//
//        try {
//            var0.load(ArchitectView.class.getClassLoader().getResourceAsStream("assets/sdk.properties"));
//        } catch (Exception var2) {
//            Log.w(l, "Cannot open properties file, use defaults");
//        }
//
//        return var0;
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void removeArchitectJavaScriptInterfaceListener(ArchitectJavaScriptInterfaceListener architectJavaScriptInterfaceListener) {
//        this.G.remove(architectJavaScriptInterfaceListener);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public void addArchitectJavaScriptInterfaceListener(ArchitectJavaScriptInterfaceListener architectJavaScriptInterfaceListener) {
//        this.G.add(architectJavaScriptInterfaceListener);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public Set<ArchitectJavaScriptInterfaceListener> getArchitectJavaScriptInterfaceListenerSet() {
//        return this.G;
//    }
//
//    public native void createNative();
//
//    public native void destroyNative();
//
//    private native void createARchitectCore(IArchitectCallbackListener var1, ServiceManagerInternal var2, AssetManager var3, String var4, String var5, int var6, String var7, boolean var8);
//
//    native void destroyEngine();
//
//    private native void clearCacheInternal();
//
//    native void loadingFinished();
//
//    native String getArchitectVersion();
//
//    native void loadingStarted();
//
//    native void setNetworkStatus(String var1);
//
//    native void setCameraMirroring(boolean var1);
//
//    private native void setKey(String var1, String var2);
//
//    private native void setOrigin(String var1);
//
//    private native String getLicenseType();
//
//    private native String getCustomerMail();
//
//    private native boolean getShowIcon();
//
//    private native boolean getShowSplash();
//
//    private native boolean getDoTracking();
//
//    native void setHardwareConfiguration(String var1);
//
//    private native void setCloudTrackerURL(String var1);
//
//    private native boolean sendUsageTrackingRequest(String var1, String var2, String var3, String var4, String var5, float var6);
//
//    private native String trackingPlatformIdentifier();
//
//    private native String trackingOriginIdentifierFromString(String var1);
//
//    private native long getArchitectSdkPtr();
//
//    public long getNativePtr() {
//        return this.nativePtr;
//    }
//
//    private native long getPluginManagerPtr();
//
//    private native long getRendererInterfacePtr();
//
//    private native void forwardTouchBeganEvent(int var1, float var2, float var3);
//
//    private native void forwardTouchChangedEvent(int[] var1, float[] var2, float[] var3);
//
//    private native void forwardTouchEndedEvent(int var1, float var2, float var3);
//
//    private native void forwardTouchCancelledEvent(int var1, float var2, float var3);
//
//    static {
//        System.loadLibrary("architect");
//        com.wikitude.common.debug.internal.a.a(false);
//        DAssert.a(false);
//        d = true;
//        j = new ArchitectSDKBuildInformationInternal();
//        k = new com.wikitude.common.permission.internal.a();
//        l = ArchitectView.class.getSimpleName();
//        v = j();
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public interface CaptureScreenCallback {
//        @com.wikitude.common.annotations.internal.a
//        int CAPTURE_MODE_CAM = 0;
//        @com.wikitude.common.annotations.internal.a
//        int CAPTURE_MODE_CAM_AND_WEBVIEW = 1;
//
//        @com.wikitude.common.annotations.internal.a
//        void onScreenCaptured(Bitmap var1);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public interface ArchitectWorldLoadedListener {
//        @com.wikitude.common.annotations.internal.a
//        void worldWasLoaded(String var1);
//
//        @com.wikitude.common.annotations.internal.a
//        void worldLoadFailed(int var1, String var2, String var3);
//    }
//
//    /** @deprecated */
//    @Deprecated
//    @com.wikitude.common.annotations.internal.a
//    public interface ArchitectUrlListener {
//        /** @deprecated */
//        @Deprecated
//        boolean urlWasInvoked(String var1);
//    }
//
//    @com.wikitude.common.annotations.internal.a
//    public interface SensorAccuracyChangeListener {
//        @com.wikitude.common.annotations.internal.a
//        void onCompassAccuracyChanged(int var1);
//    }
//
//    static class NetworkStateReceiver extends BroadcastReceiver {
//        private static final String a = "NetworkStateReceiver";
//        private final ArchitectView b;
//
//        public NetworkStateReceiver(ArchitectView architectView) {
//            this.b = architectView;
//        }
//
//        public void onReceive(Context context, Intent intent) {
//            Log.d("NetworkStateReceiver", "Network connectivity change");
//            if(intent.getExtras() != null) {
//                ConnectivityManager var3 = (ConnectivityManager)context.getSystemService("connectivity");
//                NetworkInfo var4 = var3.getActiveNetworkInfo();
//                if(var4 != null && var4.isConnectedOrConnecting()) {
//                    if(var4.getType() == 1) {
//                        this.b.setNetworkStatus(ARView.NetworkStatus.WIFI.name());
//                    } else {
//                        this.b.setNetworkStatus(ARView.NetworkStatus.MOBILE.name());
//                    }
//                } else {
//                    this.b.setNetworkStatus(ARView.NetworkStatus.NONE.name());
//                }
//            }
//
//        }
//    }
//
//    static enum NetworkStatus {
//        NONE,
//        WIFI,
//        MOBILE;
//
//        private NetworkStatus() {
//        }
//    }
//
//    static enum ViewState {
//        constructed,
//        onPostCreate,
//        onResume,
//        onPause,
//        onDestroy,
//        initFailed,
//        undefined;
//
//        private ViewState() {
//        }
//    }
//
//    public static class LibraryLoadFailedException extends ArchitectView.ArchitectInitializeException {
//        private static final long serialVersionUID = -3436549205240611293L;
//
//        public LibraryLoadFailedException(String msg) {
//            super(msg);
//        }
//    }
//
//    public static class MissingFeatureException extends ArchitectView.ArchitectInitializeException {
//        private static final long serialVersionUID = -1120070352130259205L;
//
//        public MissingFeatureException(Exception e) {
//            super(e);
//        }
//
//        public MissingFeatureException(String msg) {
//            super(msg);
//        }
//    }
//
//    public static class CamNotAccessibleException extends ArchitectView.ArchitectInitializeException {
//        private static final long serialVersionUID = -2060234091280507469L;
//
//        public CamNotAccessibleException(Exception e) {
//            super(e);
//        }
//
//        public CamNotAccessibleException(String msg) {
//            super(msg);
//        }
//    }
//
//    public abstract static class ArchitectInitializeException extends RuntimeException {
//        private static final long serialVersionUID = 3315635385062334407L;
//
//        public ArchitectInitializeException(Exception e) {
//            super(e);
//        }
//
//        public ArchitectInitializeException(String msg) {
//            super(msg);
//        }
//    }
//}
