// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using UnityEngine;

namespace Google.XR.XDTK {
    public class UDPTransceiver : Transceiver
    {
        public GameObject DevicePrefab;
        
        // Listener Variables (RX)
        private int listenerPort = 5555;
        private UdpClient receiver;
        private IPEndPoint receiverIP;
        private int receiveBufferSize = 120000;
        private object obj = null;
        private System.AsyncCallback AC;
        private byte[] receivedBytes;
        private string receivedString;
        private string receivedIPaddress;

        // Sender variables (TX)
        private  int senderPort = 5556;
        private UdpClient sender;
        private IPEndPoint senderIPEndPoint;

        // Device Discovery
        private int nextID = 0;
        private bool creatingNewDevice = false;
        private string addressforCreatedDevice = "";
        private int IDforCreatedDevice = -1;
        private string infoMessageforCreatedDevice = "";

        // Debug
        public bool debugPrint = false;

        // Start is called before the first frame update
        public override void Start()
        {
            // Initialize Transceiver
            base.Initialize();

            // UDP specific setup
            InitializeUDPListener();
            InitializeUDPSender();

            // Remove any improper IP addresses and IDs from Device scripts
            RemoveInvalidAddressesAndIDs();
            RemoveDuplicateAddressesAndIDs();
            InitializeDevices();
        }

        // Update is called once per frame
        public override void Update()
        {
            // Handle creating new device (must be done on main thread)
            if (creatingNewDevice) {
                CreateNewDevice(IDforCreatedDevice,addressforCreatedDevice,infoMessageforCreatedDevice);
                creatingNewDevice = false;
            }
        }

        void InitializeUDPListener()
        {
            receiverIP = new IPEndPoint(IPAddress.Any, listenerPort);
            receiver = new UdpClient();
            receiver.Client.ReceiveBufferSize = receiveBufferSize;
            receiver.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, optionValue: true);
            receiver.ExclusiveAddressUse = false;
            receiver.EnableBroadcast = true;
            receiver.Client.Bind(receiverIP);
            receiver.DontFragment = true;

            // Set callback for UDP listener
            AC = new System.AsyncCallback(ReceivedUDPPacket);
            receiver.BeginReceive(AC, obj);
            Debug.Log("[UDPTransceiver] UDP Listener initialized.");
        }

        void InitializeUDPSender() {
            sender = new UdpClient();
            Debug.Log("[UDPTransceiver] UDP Sender initialized.");
        }

        void RemoveDuplicateAddressesAndIDs() {
            List<string> addresses = new List<string>();
            List<int> IDs = new List<int>();
            foreach (Device d in FindObjectsOfType<Device>()) {
                // Check for duplicate addresses
                if (!string.IsNullOrEmpty(d.Address) && addresses.Contains(d.Address)) {
                    Debug.LogWarning("[UDPTransceiver] Duplicate device address " + d.Address + ". Removing one.");
                    d.Address = null;
                } else {
                    addresses.Add(d.Address);
                }

                // Check for duplicate IDs
                if (d.ID > 0 && IDs.Contains(d.ID)) {
                    Debug.LogWarning("[UDPTransceiver] Duplicate device ID " + d.ID + ". Removing one.");
                    d.ID = -1;
                } else {
                    IDs.Add(d.ID);
                }
            }
        }

        void RemoveInvalidAddressesAndIDs() {
            foreach (Device d in FindObjectsOfType<Device>()) {
                // Check for invalid addresses
                if (!string.IsNullOrEmpty(d.Address) && !IPAddress.TryParse(d.Address, out _)) {
                    Debug.LogWarning("[UDPTransceiver] Device address " + d.Address + " is not a valid IPv4 address. Removing.");
                    d.Address = null;
                }

                // Check for invalid IDs
                if (d.ID < -1) {
                    Debug.LogWarning("[UDPTransceiver] Device ID " + d.ID + " is not a valid. Removing.");
                    d.ID = -1;
                }
            }
        }

        // Add Devices in scene to to database 
        void InitializeDevices() {
            foreach (Device d in FindObjectsOfType<Device>()) {
                // Add to database
                if (!string.IsNullOrEmpty(d.Address)) devicesByAddress.Add(d.Address,d);
                if (d.ID >= 0) devicesByID.Add(d.ID,d);
                devices.Add(d);
            }
        }

        // "Message received" callback (Android --> Unity)
        void ReceivedUDPPacket(System.IAsyncResult result)
        {
            // Convert message to string
            receivedBytes = receiver.EndReceive(result, ref receiverIP);
            receivedString = System.Text.Encoding.UTF8.GetString(receivedBytes);
            receivedIPaddress = receiverIP.Address.ToString();
            if (debugPrint) Debug.Log("[UDPTransceiver] Received message: " + receivedString);

            // Handle device discovery
            if (!registeredAddresses.Contains(receivedIPaddress)) {
                // If  we haven't heard from this device before, handle adding it
                Debug.Log("[UDPTransceiver] Attempting to add device: " + receivedIPaddress);
                HandleAddDevice(receivedString,receivedIPaddress);
            } 
            
            if (registeredAddresses.Contains(receivedIPaddress)) {
                // Route message to proper device script
                base.RouteMessageToDevice(receivedString,receivedIPaddress);

                // Send HEARTBEAT back to sender
                SendMessage("HEARTBEAT", receivedIPaddress);
            }

            receiver.BeginReceive(AC, obj);
        }

        // Add 
        void HandleAddDevice(string message, string address) {
            // parse message
            string[] strings = message.Split(',');
            long timeStamp = long.Parse(strings[0]);

            // get message header
            if (!(strings.Length > 1)) return;
            string header = strings[1];

            // if this is a DEVICE_INFO message, add the device
            if (header == "DEVICE_INFO") {
                Debug.Log("[UDPTransceiver] Received DEVICE_INFO message from: " + address);

                // Check if this IP address has been specified by any Device scripts in the scene
                if (devicesByAddress.ContainsKey(address)) {
                    
                    // if so, add to registered addresses
                    registeredAddresses.Add(address);

                    // check if we need to generate an ID or if it has been specified
                    Device d = devicesByAddress[address];
                    if (d.ID < 0) {
                        // assign new ID
                        while (devicesByID.ContainsKey(nextID)) nextID++;
                        d.ID = nextID;
                        nextID++;
                    }

                    // Add to ID database if needed
                    if (!devicesByID.ContainsKey(d.ID)) {
                        devicesByID.Add(d.ID,d);
                    }

                    Debug.Log("[UDPTransceiver] Added Device " + d.ID + ": " + address);   
                    return;
                } 
                else {
                    // check if there are any devices in the scene with a specified ID (and no specified address)
                    foreach (Device d in devices) {
                        // if we come across one, add it
                        if (d.ID >= 0 && string.IsNullOrEmpty(d.Address)) {
                            d.Address = address;

                            // Add to databases
                            if (!devicesByID.ContainsKey(d.ID)) devicesByID.Add(d.ID,d);
                            if (!devicesByAddress.ContainsKey(d.Address)) devicesByAddress.Add(d.Address,d);
                            registeredAddresses.Add(d.Address);
                            Debug.Log("[UDPTransceiver] Added Device " + d.ID + ": " + address);       
                            return;
                        }
                    }

                    // if there are any devices in the scene, even with no specified ID or address
                    // assign those before instantiating any others
                    foreach (Device d in devices) {
                        // if we come across one, add it
                        if (d.ID < 0 && string.IsNullOrEmpty(d.Address)) {
                            d.Address = address;

                            // assign new ID
                            while (devicesByID.ContainsKey(nextID)) nextID++;
                            d.ID = nextID;
                            nextID++;

                            // Add to databases
                            if (!devicesByID.ContainsKey(d.ID)) devicesByID.Add(d.ID,d);
                            if (!devicesByAddress.ContainsKey(d.Address)) devicesByAddress.Add(d.Address,d);
                            registeredAddresses.Add(d.Address);
                            Debug.Log("[UDPTransceiver] Added Device " + d.ID + ": " + address);       
                            return;
                        }
                    }
                }

                // If we reach this point in the script, that means we need to instantiate a new Device (on the main thread)
                Debug.Log("[UDPTransceiver] Instantiating new Device prefab.");
                if (!creatingNewDevice) {
                    creatingNewDevice = true;

                    // assign address
                    addressforCreatedDevice = address;

                    // assign new ID
                    while (devicesByID.ContainsKey(nextID)) nextID++;
                    IDforCreatedDevice = nextID;                
                    nextID++;

                    // store message
                    infoMessageforCreatedDevice = message;
                }
            } 
            // otherwise, request DEVICE_INFO from this device
            else {
                SendMessage("WHOAREYOU", address);
                Debug.Log("[UDPTransceiver] Sent device info request to: " + address);
            }
        }

        // Create a new Device prefab and store its information
        void CreateNewDevice(int newID, string newAddress, string newInfoMessage){
            // Create Device
            GameObject d_object = Instantiate(DevicePrefab);
            Device d = d_object.GetComponent<Device>();
            d.ID = IDforCreatedDevice;
            d.Address = addressforCreatedDevice;

            // Add to database
            devices.Add(d);
            registeredAddresses.Add(d.Address);
            if (!devicesByID.ContainsKey(d.ID)) devicesByID.Add(d.ID,d);
            if (!devicesByAddress.ContainsKey(d.Address)) devicesByAddress.Add(d.Address,d);

            // Route DEVICE_INFO message to newly created device
            base.RouteMessageToDevice(infoMessageforCreatedDevice,d.Address);
        }

        // Send message to specific IP address (Unity --> Android)
        public void SendMessage(string message, string IPAddressToSendTo)
        {
            senderIPEndPoint = new IPEndPoint(IPAddress.Parse(IPAddressToSendTo), senderPort);
            byte[] data = Encoding.UTF8.GetBytes(message);
            sender.Send(data, data.Length, senderIPEndPoint);
        }

        void OnDestroy()
        {
            receiver?.Close();
            sender?.Close();
        }
    }
}