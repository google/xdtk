using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ControlPoseARCore : MonoBehaviour
{
    public int deviceToListenTo = 0;
    public GameObject referenceObject;
    Vector3 positionOffset = Vector3.zero;
    Quaternion rotationOffset = Quaternion.identity;
    Device device;

    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        UpdateTargetDevice();

        if (device != null) {
            // Update Position
            this.transform.position = device.PositionAR - positionOffset;

            // Update Orientation
            this.transform.rotation = Quaternion.AngleAxis(180f,Vector3.up) * device.RotationAR * rotationOffset;

            if (Input.GetKeyDown(KeyCode.C)) {
                CalibratePose();
            }
        }
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

    public void CalibratePose()
        {
            // Reset Pose
            positionOffset = Vector3.zero;
            rotationOffset = Quaternion.identity;
            this.transform.position = device.PositionAR - positionOffset;
            this.transform.rotation = device.RotationAR * rotationOffset;

            // Get Position Offset
            positionOffset = device.PositionAR - referenceObject.transform.position;
            
            // Get Rotation Offset
            // grab difference between object and reference [1]
            // [1]: https://forum.unity.com/threads/get-the-difference-between-two-quaternions-and-add-it-to-another-quaternion.513187/
            rotationOffset = Quaternion.Inverse(Quaternion.AngleAxis(180f,Vector3.up) * device.RotationAR) * referenceObject.transform.rotation;
        }
}
