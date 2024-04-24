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

public class MoveOnDragXZ : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public float speed = 0.01f;
    int touchID = 0;
    Device device;
    MultiDeviceEventManager eventManager;

    private void OnEnable() {
        eventManager.OnTouchDown.AddListener(OnTouchDown);
        eventManager.OnTouchUp.AddListener(OnTouchUp);
        eventManager.OnTouchMove.AddListener(OnTouchMove);
    }

    private void OnDisable() {
        eventManager.OnTouchDown.RemoveListener(OnTouchDown);
        eventManager.OnTouchUp.RemoveListener(OnTouchUp);
        eventManager.OnTouchMove.RemoveListener(OnTouchMove);
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
        if (deviceToListenTo == ID && touchID == _touchID) {
            // Calculate rotation amount
            float dispZ = -delta.y * speed * Mathf.Deg2Rad;
            float dispX = delta.x * speed * Mathf.Deg2Rad;

            // Apply displacement
            transform.position += new Vector3 (dispX, 0f, dispZ);
        }
    } 
}
