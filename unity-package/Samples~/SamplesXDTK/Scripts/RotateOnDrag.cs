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
using Google.XR.XDTK;

public class RotateOnDrag : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public float speed = 5f;
    public float Zspeed = 10f;
    int touchID = 0;
    Device device;
    MultiDeviceEventManager eventManager;
    public bool pinching = false;
    float lastAngle;

    private void OnEnable() {
        eventManager.OnTouchDown.AddListener(OnTouchDown);
        eventManager.OnTouchUp.AddListener(OnTouchUp);
        eventManager.OnTouchMove.AddListener(OnTouchMove);
        eventManager.OnPinchStart.AddListener(OnPinchStart);
        eventManager.OnPinchEnd.AddListener(OnPinchEnd);
        eventManager.OnPinchMove.AddListener(OnPinchMove);
    }

    private void OnDisable() {
        eventManager.OnTouchDown.RemoveListener(OnTouchDown);
        eventManager.OnTouchUp.RemoveListener(OnTouchUp);
        eventManager.OnTouchMove.RemoveListener(OnTouchMove);
        eventManager.OnPinchStart.RemoveListener(OnPinchStart);
        eventManager.OnPinchMove.RemoveListener(OnPinchMove);
    }

    void Awake() {
        // initialize event manager
        eventManager = FindObjectOfType<MultiDeviceEventManager>();
    }

    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();
    }

    private void UpdateTargetDevice() {
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
        }
    } 

    private void OnTouchUp(int ID, int _touchID, Vector2 touchPos) {
        if (deviceToListenTo == ID && touchID == _touchID) {
        }
    } 

    private void OnTouchMove(int ID, int _touchID, Vector2 delta) {
        if (deviceToListenTo == ID && touchID == _touchID && !pinching) {
            // Calculate rotation amount
            float rotX = delta.y * speed * Mathf.Deg2Rad;
            float rotY = delta.x * speed * Mathf.Deg2Rad;

            // Apply rotation
            transform.Rotate(Vector3.up, -rotY, Space.World);
            transform.Rotate(Vector3.right, -rotX, Space.World);

            // add rotate about z with two fingers in another script
        }
    } 

    private void OnPinchStart(int ID, float span) {
        if (deviceToListenTo == ID) {
            pinching = true;

            // compute angle of pinch relative to screen
            Vector2 spanDirection = device.TouchPos[1] - device.TouchPos[0];
            lastAngle = Vector2.SignedAngle(Vector2.right, spanDirection);
        }
    } 

    private void OnPinchEnd(int ID, float span) {
        if (deviceToListenTo == ID) {
            pinching = false;
        }
    } 

    private void OnPinchMove(int ID, float span) {
        if (deviceToListenTo == ID && device.isTouched[0] && device.isTouched[1]) {
            // compute angle of pinch relative to screen
            Vector2 spanDirection = device.TouchPos[1] - device.TouchPos[0];
            float thisAngle = Vector2.SignedAngle(Vector2.right, spanDirection);


            // Apply rotation
            float rotZ = (thisAngle - lastAngle) * Zspeed * Mathf.Deg2Rad;
            transform.Rotate(Vector3.forward, -rotZ, Space.World);

            lastAngle = thisAngle;
        }
    } 
}
