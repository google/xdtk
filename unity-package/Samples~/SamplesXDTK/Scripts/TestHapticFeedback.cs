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

using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TestHapticFeedback : MonoBehaviour
{
    public Device device;
    public UDPTransceiver udp;
    public int millis = 100;
    [Range(0,255)]
    public int amplitude = 100;

    private string ip;
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        ip = device.Address;
        
        if (!string.IsNullOrEmpty(ip)) {
            if (Input.GetKeyDown(KeyCode.Alpha1)) {
                udp.SendMessage("HAPTICS_CLICK", ip);
            }

            if (Input.GetKeyDown(KeyCode.Alpha2)) {
                udp.SendMessage("HAPTICS_DOUBLE_CLICK", ip);
            }

            if (Input.GetKeyDown(KeyCode.Alpha3)) {
                udp.SendMessage("HAPTICS_HEAVY_CLICK", ip);
            }

            if (Input.GetKeyDown(KeyCode.Alpha4)) {
                udp.SendMessage("HAPTICS_TICK", ip);
            }

            if (Input.GetKeyDown(KeyCode.Alpha5)) {
                udp.SendMessage("HAPTICS_ONESHOT," + millis.ToString() + "," + amplitude.ToString(), ip);
            }
        }
    }
}
