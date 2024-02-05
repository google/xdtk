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

using UnityEngine;

public class ScaleToDevice : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public float scaleFactor = 1;
    public bool inheritDeviceFromParent = true;
    Device device;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();
        
        if (device.receivedDeviceInfo) {
            gameObject.transform.localScale = new Vector3 (scaleFactor * device.Size_m.x, scaleFactor * device.Size_m.y, transform.localScale.z);
        }
    }

    private void UpdateTargetDevice() {
        // get target device ID, if inheriting from parent
        if (inheritDeviceFromParent) {
            Device d = GetComponentInParent<Device>();
            if (d != null) deviceToListenTo = d.ID;
        }

        // find target device
        foreach (Device d in FindObjectsOfType<Device>()) {
            if (d.ID == deviceToListenTo) {
                device = d;
                break;
            }
        }
    }
}
