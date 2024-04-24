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

namespace Google.XR.XDTK {
    public class PositionCursor : MonoBehaviour
    {
        public int deviceToListenTo = 0;
        public bool inheritDeviceFromParent = true;
        public int touchID;
        public GameObject screen;
        public GameObject pen;
        Device device;
        MultiDeviceEventManager eventManager;
        MeshRenderer meshRenderer;

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
            meshRenderer = GetComponent<MeshRenderer>();
            meshRenderer.enabled = false;

            if (pen != null) pen.GetComponent<MeshRenderer>().enabled = false;
        }

        // Update is called once per frame
        void Update()
        {
            UpdateTargetDevice();
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
                UpdateCurosorPosition();

                if (device.ToolType == Device.Tool.Pen && pen != null) {
                    pen.GetComponent<MeshRenderer>().enabled = true;
                }
                meshRenderer.enabled = true;
            }
        } 

        private void OnTouchUp(int ID, int _touchID, Vector2 touchPos) {
            if (deviceToListenTo == ID && touchID == _touchID) {
                UpdateCurosorPosition();

                if (device.ToolType == Device.Tool.Pen && pen != null) {
                    pen.GetComponent<MeshRenderer>().enabled = false;
                }
                meshRenderer.enabled = false;
            }
        } 

        private void OnTouchMove(int ID, int _touchID, Vector2 delta) {
            if (deviceToListenTo == ID && touchID == _touchID) {
                UpdateCurosorPosition();
            }
        } 

        private void UpdateCurosorPosition() {
            var xProp = device.TouchPos[touchID].x / device.Size_px.x;
            var yProp = device.TouchPos[touchID].y / device.Size_px.y;

            transform.localPosition = new Vector3(-1f * xProp * screen.transform.localScale.x + screen.transform.localScale.x/2f,
                                                -1f * yProp * screen.transform.localScale.y + screen.transform.localScale.y/2f,
                                                1f * screen.transform.localScale.z/2f);
        }
    }
}