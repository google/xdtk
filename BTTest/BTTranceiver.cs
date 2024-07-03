using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using InTheHand.Net.Sockets;
using InTheHand.Net.Bluetooth;

public class BTTranceiver : MonoBehaviour
{
    private BluetoothClient client;
    private IReadOnlyCollection<BluetoothDeviceInfo> devices;

    // Start is called before the first frame update
    void Start()
    {
        client = new BluetoothClient();
    }

    // Update is called once per frame
    void Update()
    {
        client = new BluetoothClient();
        IReadOnlyCollection<BluetoothDeviceInfo> devices = client.DiscoverDevices();
        Debug.Log(devices);
    }
}
