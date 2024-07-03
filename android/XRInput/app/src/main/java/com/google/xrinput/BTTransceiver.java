package com.google.xrinput;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.ar.core.examples.java.common.helpers.BTPermissionHelper;

public class BTTransceiver {
    private final String TAG = BTTransceiver.class.getSimpleName();

    // Context
    private Activity activity;

    // Grab a Bluetooth Manager and Adapter
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    public BTTransceiver(Activity activity){
        Log.d(TAG, "Setting up Bluetooth Transceiver...");

        // Setup Context
        this.activity = activity;

        // setup BT manager and adapter
        btManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        becomeBLEDiscoverable();
    }

    private void becomeBLEDiscoverable() {
        if (btAdapter == null){
            // Device does not support Bluetooth
            // TODO: Handle this case!
        } else if (!btAdapter.isEnabled()){
            // Bluetooth is not enabled
            // TODO: Ask it to enable Bluetooth
            Log.d(TAG, "Bluetooth isn't enabled. Asking for enable...");
            BTPermissionHelper.askToEnableBT(activity);
        } else {

        }
    }
}
