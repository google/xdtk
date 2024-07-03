package com.google.ar.core.examples.java.common.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.xrinput.BTTransceiver;

public final class BTPermissionHelper {
    private static final int BT_PERMISSION_CODE = 1;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private static final String TAG = BTPermissionHelper.class.getSimpleName();

    /** Checks if a single permission is granted */
    public static boolean isPermissionGranted (Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasBTPermission(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return isPermissionGranted(activity, android.Manifest.permission.BLUETOOTH_SCAN) &&
                    isPermissionGranted(activity, android.Manifest.permission.BLUETOOTH_CONNECT);
        } else{
            return isPermissionGranted(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    private static void requestBluetoothPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                },
                BT_PERMISSION_CODE
        );
    }

    /** Same as above, but for older device APIs */
    private static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                BT_PERMISSION_CODE
        );
    }

    /** Request for the relevant permissions. */
    public static void requestPermissions(Activity activity) {
        Log.d(TAG, "Requesting Permissions...");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            requestBluetoothPermissions(activity);
        } else{
            requestLocationPermission(activity);
        }
    }

    /** Check to see if we need to show the rationale for this permission. */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.BLUETOOTH_SCAN) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.BLUETOOTH_CONNECT);
        } else{
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
