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

public class RotateWithDevice : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public bool inheritDeviceFromParent = true;
    private Device device;
    public enum SensorType {
        GYRO,
        ARCORE
    }
    public SensorType sensor = SensorType.GYRO;

    [Tooltip("Rotational offset about world up direction.")]
    public float rotationOffset;


    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();

        if (sensor == SensorType.GYRO) {
            Quaternion GyroOrientation = device.GameRotationVector;
            transform.localRotation = Quaternion.AngleAxis(rotationOffset,Vector3.up) * GyroOrientation;
        }

        else if (sensor == SensorType.ARCORE) {
            Quaternion ARcoreOrientation = device.RotationAR;
            transform.localRotation = Quaternion.AngleAxis(rotationOffset,Vector3.up) * ARcoreOrientation;
        }

        // DEBUG
        if (Input.GetKeyDown(KeyCode.Space) && device.isTouched[0]) CalibrateForwardDirection();
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

    private void CalibrateForwardDirection() {
        Vector3 deviceYProjectedOntoHorizontalPlane = Vector3.ProjectOnPlane(transform.up, Vector3.up);
        
        var offsetFromForward = Vector3.SignedAngle(deviceYProjectedOntoHorizontalPlane, Vector3.forward, Vector3.up);

        rotationOffset += offsetFromForward;
    }


}
