using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScaleOnPinch : MonoBehaviour
{
    public int deviceToListenTo = 0;
    MultiDeviceEventManager eventManager;
    bool pinching = false;
    Device device;
    float startSpan;
    Vector3 startScale;

    private void OnEnable() {
        eventManager.OnPinchStart.AddListener(OnPinchStart);
        eventManager.OnPinchEnd.AddListener(OnPinchEnd);
    }

    private void OnDisable() { 
        eventManager.OnPinchStart.RemoveListener(OnPinchStart);
        eventManager.OnPinchEnd.RemoveListener(OnPinchEnd);
    }

    void Awake() {
        // initialize event manager
        eventManager = FindObjectOfType<MultiDeviceEventManager>();
    }

    void Update() {
        UpdateTargetDevice();

        if (pinching && device != null) {
            var scaleFactor = (device.PinchSpan / startSpan);
            gameObject.transform.localScale = scaleFactor * startScale;
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

    private void OnPinchStart(int ID, float span) {
        if (deviceToListenTo == ID) {
            pinching = true;
            startSpan = device.PinchSpan;
            startScale = gameObject.transform.localScale;
        }
    } 

    private void OnPinchEnd(int ID, float span) {
        if (deviceToListenTo == ID) {
            pinching = false;
        }
    } 
}
