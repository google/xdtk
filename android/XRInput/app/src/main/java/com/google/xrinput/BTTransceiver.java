package com.google.xrinput;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.ar.core.examples.java.common.helpers.BTPermissionHelper;

import java.util.UUID;

public class BTTransceiver {
    private final String TAG = BTTransceiver.class.getSimpleName();

    // Context
    private Activity activity;

    // Grab a Bluetooth Manager and Adapter
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    private BluetoothLeAdvertiser bleAdvertiser;

    private BluetoothGattServer gattServer;

    public BTTransceiver(Activity activity){
        Log.d(TAG, "Setting up Bluetooth Transceiver...");

        // Setup Context
        this.activity = activity;

        // setup BT manager and adapter
        btManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
    }

    private boolean isBluetoothEnabled(){
        // The former means that BT is not available on device
        // The latter means that BT is not enabled on device
        return btAdapter == null || !btAdapter.isEnabled();
    }

    private void askEnableBluetooth(){
        if (btAdapter == null){
            // Device does not support Bluetooth
            // TODO: Handle this case!
        } else if (!btAdapter.isEnabled()){
            // Bluetooth is not enabled
            Log.d(TAG, "Bluetooth isn't enabled. Asking to enable it...");
            BTPermissionHelper.askToEnableBT(activity);
        }
    }

    // An advertising callback used to deliver the operation's status
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            Log.e("AdvertiseCallback", "Start Advertise Callback Failed");
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i("AdvertiseCallback", "Start Advertise Callback Success");
        }

        ;
    };

    private final BLEGATTServerCallback mGattServerCallback = new BLEGATTServerCallback(gattServer);

    @SuppressLint("MissingPermission")
    public void startAdvertise() {
        btAdapter.setName("PeripheralAndroid"); //8 characters works, 9+ fails

        gattServer = btManager.openGattServer(activity, mGattServerCallback);

        final String SERVICE_A = "0000fff0-0000-1000-8000-00805f9b34fb";
        final String CHAR_READ_1 = "00fff1-0000-1000-8000-00805f9b34fb";
        final String CHAR_READ_2 = "00fff2-0000-1000-8000-00805f9b34fb";
        final String CHAR_WRITE = "00fff3-0000-1000-8000-00805f9b34fb";


        BluetoothGattCharacteristic read1Characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_READ_1),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        read1Characteristic.setValue(new String("this is read 1").getBytes());

        BluetoothGattCharacteristic read2Characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_READ_2),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        read2Characteristic.setValue(new String("this is read 2").getBytes());


        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_WRITE),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        );


        BluetoothGattService AService = new BluetoothGattService(
                UUID.fromString(SERVICE_A),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        AService.addCharacteristic(read1Characteristic);
        AService.addCharacteristic(read2Characteristic);
        AService.addCharacteristic(writeCharacteristic);

        // Add notify characteristic here !!!

        gattServer.addService(AService);


        bleAdvertiser.startAdvertising(settingBuilder.build(),
                advBuilder.build(), advertiseCallback);

    }

    @SuppressLint("MissingPermission")
    public void stopAdvertise() {
        if (bleAdvertiser != null){
            bleAdvertiser.stopAdvertising(advertiseCallback);
        }

        bleAdvertiser = null;
    }
}
