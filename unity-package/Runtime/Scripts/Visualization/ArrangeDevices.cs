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
using UnityEngine;

public class ArrangeDevices : MonoBehaviour
{
    public float spacing = 0.04f;
    public Vector3 origin = Vector3.zero;
    public Vector3 spanDirection = Vector3.right;
    private List<Device> devices;
    private Transceiver transceiver;
    protected Dictionary<int, Device> devicesByID;

    // Start is called before the first frame update
    void Start()
    {
        transceiver = FindObjectOfType<Transceiver>();
    }

    // Update is called once per frame
    void Update()
    {
        // Setup
        devices = transceiver.GetDevices();
        devicesByID = transceiver.GetDevicesByID();
        List<int> IDs = transceiver.GetIDs();
        IDs.Sort();

        // Get number of devices
        int numDevices = devices.Count;
        float totalDeviceWidth = 0f;
        foreach (Device d in devices) {
            totalDeviceWidth += d.Size_m.x;
        }

        if (numDevices > 0) {
            // Compute position of first device
            List<Vector3> newPositions = new List<Vector3>();
            int numSpaces = numDevices - 1;
            float firstDeviceWidth = devicesByID[IDs[0]].Size_m.x;
            Vector3 startingPos = -0.5f * (numSpaces * spacing + totalDeviceWidth - firstDeviceWidth) * spanDirection + origin;
            devicesByID[IDs[0]].gameObject.transform.position = startingPos;
            for(int i = 1; i < numDevices; i++) {
                Device thisDevice = devicesByID[IDs[i]];

                // position this device
                Vector3 lastDevicePos = devicesByID[IDs[i-1]].gameObject.transform.position;
                float lastWidth = devicesByID[IDs[i-1]].Size_m.x;
                float thisWidth = devicesByID[IDs[i]].Size_m.x;
                float displacementFromLastDevice = 0.5f * (lastWidth + thisWidth) + spacing;
                thisDevice.gameObject.transform.position = lastDevicePos + displacementFromLastDevice * spanDirection;
            }
        }

        
    }
}
