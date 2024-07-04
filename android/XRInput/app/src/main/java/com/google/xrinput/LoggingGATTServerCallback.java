package com.google.xrinput;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

// This replaces the normal gatt server callback with a bunch of logs. No need to use RN
// Source: https://gist.github.com/bitristan/80705230ec59d952414f
public class LoggingGATTServerCallback extends BluetoothGattServerCallback {
    // the GATT server
    private BluetoothGattServer gattServer;

    public LoggingGATTServerCallback(BluetoothGattServer gattServer){
        this.gattServer = gattServer;
    }

    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        Log.d("GattServer", "Our gatt server connection state changed, new state ");
        Log.d("GattServer", Integer.toString(newState));
        super.onConnectionStateChange(device, status, newState);
    }

    public void onServiceAdded(int status, BluetoothGattService service) {
        Log.d("GattServer", "Our gatt server service was added.");
        super.onServiceAdded(status, service);
    }

    @SuppressLint("MissingPermission") // At this point permissions have been repeatedly checked
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        Log.d("GattServer", "Our gatt characteristic was read.");
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                characteristic.getValue());
    }

    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        Log.d("GattServer", "We have received a write request for one of our hosted characteristics");
        Log.d("GattServer", "data = " + value.toString());
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
    }

    public void onNotificationSent(BluetoothDevice device, int status) {
        Log.d("GattServer", "onNotificationSent");
        super.onNotificationSent(device, status);
    }

    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        Log.d("GattServer", "Our gatt server descriptor was read.");
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
    }

    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        Log.d("GattServer", "Our gatt server descriptor was written.");
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        Log.d("GattServer", "Our gatt server on execute write.");
        super.onExecuteWrite(device, requestId, execute);
    }
}
