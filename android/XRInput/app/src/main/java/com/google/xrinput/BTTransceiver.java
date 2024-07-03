package com.google.xrinput;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BTTransceiver {
    private final String TAG = BTTransceiver.class.getSimpleName();

    // Context
    private Context context;

    // Grab a Bluetooth Manager and Adapter
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    public BTTransceiver(Context context){
        Log.d(TAG, "Setting up Bluetooth Transceiver...");

        // Setup Context
        this.context = context;

        // setup BT manager and adapter
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        becomeBLEDiscoverable();
    }

    private void becomeBLEDiscoverable() {

    }
}
