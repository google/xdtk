package com.google.xrinput;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;

// This is just a simple implementation
// Literally doesnt do anything special
public class SimpleGATTServerCallback extends BluetoothGattServerCallback {

    public SimpleGATTServerCallback (){
        super();

    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
    }
}
