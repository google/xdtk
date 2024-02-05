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

public class ScaleWithPressure : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public bool inheritDeviceFromParent = true;
    public int touchID;
    public bool scaleX = true;
    public bool scaleY = true;
    public bool scaleZ = true;
    public float minTouchSize = 0.01f;
    public float maxTouchSize = 0.1f;
    public float minScaleFactor = 0.5f;
    public float maxScaleFactor = 2f;   
    bool penActive = false; 
    
    Device device;
    MultiDeviceEventManager eventManager;
    bool isTouched;
    Vector3 originalScale;
    

    private void OnEnable() {
        eventManager.OnTouchDown.AddListener(OnTouchDown);
        eventManager.OnTouchUp.AddListener(OnTouchUp);
    }

    private void OnDisable() {
        eventManager.OnTouchDown.RemoveListener(OnTouchDown);
        eventManager.OnTouchUp.RemoveListener(OnTouchUp);
    }

    void Awake() {
        // initialize event manager
        eventManager = FindObjectOfType<MultiDeviceEventManager>();
    }

    // Start is called before the first frame update
    void Start()
    {
        originalScale = transform.localScale;
    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();

        if (isTouched) {
            float scaleFactorX = 1f;
            float scaleFactorY = 1f;
            float scaleFactorZ = 1f;

            float touchSize = device.TouchSize[touchID];
            float t = (touchSize - minTouchSize) / (maxTouchSize - minTouchSize);

            // override if pen is active
            if (penActive) t = device.Pressure;

            if (scaleX) scaleFactorX = Mathf.Lerp(minScaleFactor,maxScaleFactor,t);
            if (scaleY) scaleFactorY = Mathf.Lerp(minScaleFactor,maxScaleFactor,t);
            if (scaleZ) scaleFactorZ = Mathf.Lerp(minScaleFactor,maxScaleFactor,t);

            transform.localScale = new Vector3 (scaleFactorX * originalScale.x,
                                                scaleFactorY * originalScale.y,
                                                scaleFactorZ * originalScale.z);

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

    private void OnTouchDown(int ID, int _touchID, Vector2 touchPos) {
        if (deviceToListenTo == ID && touchID == _touchID) {
            isTouched = true;

             if (device.ToolType == Device.Tool.Pen) {
                penActive = true;
            }
        }
    } 

    private void OnTouchUp(int ID, int _touchID, Vector2 touchPos) {
        if (deviceToListenTo == ID && touchID == _touchID) {
            isTouched = false;

            if (device.ToolType == Device.Tool.Pen) {
                penActive = false;
            }
        }
    } 
}
