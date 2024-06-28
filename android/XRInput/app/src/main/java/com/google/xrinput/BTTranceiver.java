package com.google.xrinput;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.app.Activity;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTTranceiver extends Activity {
    private final String TAG = BTTranceiver.class.getSimpleName();

    // Receives BluetoothManager class from Context.
    private BluetoothManager bluetoothManager;

    // Receives BluetoothAdapter class from BluetoothManager.
    private BluetoothAdapter bluetoothAdapter;

    private String toConnectName;
    private String toConnectDeviceHardwareAddress;

    // Placeholder value for requests
    private final static int REQUEST_ENABLE_BT = 1;

    private Context BTContext;
    private Activity BTActivity;

    // used later to detect bt devices
    private BroadcastReceiver receiver;

    public BTTranceiver(Context context, Activity activity) {
        // set context and activity
        BTContext = context;
        BTActivity = activity;

        // set bt manager and grab adapter
        bluetoothManager = (BluetoothManager) BTContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // if its null we know it doesnt support, return
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            // We do something here, for now it's an error log and return
            Log.d(TAG, "Device does not support Bluetooth");
            return;
        }


        if (ActivityCompat.checkSelfPermission(BTContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // This will do something if the permission is not granted
            // For now, this just creates a log and returns
            Log.d(TAG, "Permissions for Bluetooth Connect denied.");
            return;
        }

        // require Bluetooth to be turned on if it isn't
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BTActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Create a BroadcastReceiver for ACTION_FOUND.
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // This will do something if the permission is not granted
                        // For now, this just creates a log and returns
                        Log.d(TAG, "Permissions for Bluetooth Connect denied.");
                        return;
                    }
                    toConnectName = device.getName();
                    toConnectDeviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public void connect() {
        /*
        // This is the starting code to make automatic connections to past devices. For now ask user to just reconnect every time
        // on click, first we search for BT connection. If connected, we drop it, else we ask it to connect
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // This will do something if the permission is not granted
            // For now, this just creates a log
            Log.d(TAG, "Permissions for Bluetooth Connect denied.");
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        */

        // Enables discovery and connection of new devices.
        if (ActivityCompat.checkSelfPermission(BTContext, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // This will do something if the permission is not granted
            // For now, this just creates a log and returns
            Log.d(TAG, "Permissions for Bluetooth Scan denied.");
            return;
        }

        bluetoothAdapter.startDiscovery();

        ConnectThread connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(toConnectDeviceHardwareAddress), bluetoothAdapter);

    }
}