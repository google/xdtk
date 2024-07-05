package com.google.xrinput;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

// This is just a simple implementation
// Literally doesnt do anything special
public class SimpleGATTServerCallback extends BluetoothGattServerCallback {

    public SimpleGATTServerCallback (){
        super();

    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

        Log.d("BluetoothGattServerCallback", "Asking to read characteristic");
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        Log.d("BluetoothGattServerCallback", "Asking to write characteristic");
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);

        Log.d("BluetoothGattServerCallback", "Service added with UUID " + service.getUuid());
    }
}
