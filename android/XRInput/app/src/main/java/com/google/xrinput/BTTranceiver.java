package com.google.xrinput;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Activity;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class BTTranceiver {
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

    public BTTranceiver(Context context, Activity activity) {
        BTContext = context;
        BTActivity = activity;

        bluetoothManager = (BluetoothManager) BTContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

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

        // First, require Bluetooth to be turned on if it isn't
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BTActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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

        // Create a BroadcastReceiver for ACTION_FOUND.
        final BroadcastReceiver receiver = new BroadcastReceiver() {
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

        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();
    }
}
/*
// WIP CODE

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    @SuppressLint("MissingPermission") // we have already checked in constructor for Tranceiver
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
    */