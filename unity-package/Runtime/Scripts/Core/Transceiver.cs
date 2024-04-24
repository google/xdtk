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
using System.Linq;
using UnityEngine;

namespace Google.XR.XDTK {
    public class Transceiver : MonoBehaviour
    {
        protected Dictionary<string, Device> devicesByAddress;
        protected Dictionary<int, Device> devicesByID;
        protected List<Device> devices;
        protected List<string> registeredAddresses;

        // Set up the transceiver
        // > Call this within the Start function of any Transceiver subclasses
        public void Initialize()
        {
            // Make sure no other Transceivers are in the scene
            var transceivers = FindObjectsOfType<Transceiver>();
            if (transceivers.Length > 1) {
                Debug.LogError("Cannot have more than one Transceiver (or subclass) in scene.");
            }

            // Create database of connected devices and their addresses & IDs 
            // (to be filled during device discovery)
            devices = new List<Device>();
            devicesByAddress = new Dictionary<string, Device>();
            devicesByID = new Dictionary<int, Device>();
            registeredAddresses = new List<string>();
        }

        // [Important] Call this from any Transceiver subclasses (e.g. UDPTransceiver)
        public void RouteMessageToDevice(string message, string address) {
            // make sure address is in dictionary
            if (!registeredAddresses.Contains(address)) return;
            
            var device = devicesByAddress[address];
            device.ParseData(message);
        }
        
        // Start is called before the first frame update
        public virtual void Start()
        {
            Initialize();
        }

        // Update is called once per frame
        public virtual void Update()
        {
            
        }

        public List<Device> GetDevices() {
            return devices;
        }

        public List<int> GetIDs() {
            return devicesByID.Keys.ToArray().ToList();
        }

        public Dictionary<int, Device> GetDevicesByID() {
            return devicesByID;
        }

        public Dictionary<string, Device> GetDevicesByAddress() {
            return devicesByAddress;
        }
    }
}
