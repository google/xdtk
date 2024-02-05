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
using TMPro;

public class DeviceDebugInfo : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public bool inheritDeviceFromParent = true;
    public TMP_Text deviceNameText;
    public TMP_Text deviceEventText;
    Device device;
    public GameObject screen;
    public GameObject debugCanvas;

    private float lastEventTime = 0f;

    MultiDeviceEventManager eventManager;

    private void OnEnable() {
        eventManager.OnTap.AddListener(OnTap);
        eventManager.OnDoubleTap.AddListener(OnDoubleTap);
        eventManager.OnLongPress.AddListener(OnLongPress);
        eventManager.OnPinchStart.AddListener(OnPinchStart);
        eventManager.OnPinchEnd.AddListener(OnPinchEnd);
        eventManager.OnFling.AddListener(OnFling);
    }

    private void OnDisable() {
        eventManager.OnTap.RemoveListener(OnTap);
        eventManager.OnDoubleTap.RemoveListener(OnDoubleTap);
        eventManager.OnLongPress.RemoveListener(OnLongPress);
        eventManager.OnPinchStart.RemoveListener(OnPinchStart);
        eventManager.OnPinchEnd.RemoveListener(OnPinchEnd);
        eventManager.OnFling.RemoveListener(OnFling);
    }

    void Awake() {
        // initialize event manager
        eventManager = FindObjectOfType<MultiDeviceEventManager>();
    }

    // Start is called before the first frame update
    void Start()
    {
        deviceNameText.text = "";
        deviceEventText.text = "";
    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();
        
        if (Time.realtimeSinceStartup > 2f) {
            string deviceName = device.DeviceName;
            string IP = device.Address;
            deviceNameText.text = deviceName + "\n" + IP;

            deviceNameText.transform.localPosition = new Vector3(deviceNameText.transform.localPosition.x,
                                                0.97f * (0.5f * screen.transform.localScale.y / debugCanvas.transform.localScale.y),
                                                deviceNameText.transform.localPosition.z); 

            deviceEventText.transform.localPosition = new Vector3(deviceEventText.transform.localPosition.x,
                                                -0.97f * (0.5f * screen.transform.localScale.y / debugCanvas.transform.localScale.y),
                                                deviceEventText.transform.localPosition.z); 
        }  

        if (Time.time - lastEventTime > 2f) {
            deviceEventText.text = "";
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

    private void OnTap(int ID, int touchID, Vector2 tapPos, int tapCount) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Tap";
             lastEventTime = Time.time;
        }
    } 

    private void OnDoubleTap(int ID, int touchID, Vector2 tapPos, int tapCount) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Double Tap";
             lastEventTime = Time.time;
        }
    } 

    private void OnLongPress(int ID, int touchID, Vector2 tapPos) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Long Press";
             lastEventTime = Time.time;
        }
    } 

    private void OnPinchStart(int ID, float span) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Pinch Start";
             lastEventTime = Time.time;
        }
    } 

    private void OnPinchEnd(int ID, float span) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Pinch Stop";
             lastEventTime = Time.time;
        }
    } 

    private void OnFling(int ID, Vector2 flingDirection) {
        if (deviceToListenTo == ID) {
             deviceEventText.text = "Fling";
             lastEventTime = Time.time;
        }
    } 
}
