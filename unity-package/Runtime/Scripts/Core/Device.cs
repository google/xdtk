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

using System.Collections.Generic;
using UnityEngine;

public class Device : MonoBehaviour
{
    // Device Info
    [Header("Device Info")]
    [Tooltip("ID for this device (generated based on connection order if not specified)")]
    public int ID = -1;
    [Tooltip("Identying address of device (e.g., IP). Auto-assigned to 0,1,2,..if not specified.")]
    public string Address;
    public string DeviceName;
    [Tooltip("(Width, Height) in pixels")]
    public Vector2 Size_px;
    [Tooltip("(Width, Height) in meters")]
    public Vector2 Size_m;
    [HideInInspector] public bool receivedDeviceInfo = false;

    // Sensor Info
    protected const int maxNumberOfTouchIDs = 4;

    [Header("Sensor Info")]
    [Tooltip("Latest (x,y) touch point in px")]
    public Vector2[] TouchPos = new Vector2[maxNumberOfTouchIDs];
    [Tooltip("Latest (x,y) touch delta in px")]
    public Vector2[] TouchDelta = new Vector2[maxNumberOfTouchIDs];
    public bool[] isTouched = new bool[maxNumberOfTouchIDs];
    public float[] TouchSize = new float[maxNumberOfTouchIDs];
    public int TapCount;
    public float Pressure;
    public float PinchSpan;
    public Vector3 Accelerometer = new Vector3();
    public Vector3 LinearAcceleration = new Vector3();
    public Vector3 Gravity = new Vector3();
    public Vector3 Gyroscope = new Vector3();
    [Tooltip("Android Game Rotation Vector")]
    public Quaternion GameRotationVector = new Quaternion();
    [Tooltip("Android Rotation Vector")]
    public Quaternion RotationVector = new Quaternion();
    public Vector3 MagneticField = new Vector3();
    public float Proximity;
    public float AmbientTemperature;
    public float Light;
    [Tooltip("Position from ARCore")]
    public Vector3 PositionAR = new Vector3();
    [Tooltip("Orientation from ARCore")]
    public Quaternion RotationAR = new Quaternion();
    public enum Orientation {
        Portrait,
        LandscapeLeft,
        LandscapeRight,
        PortraitUpsideDown,
        FaceUp,
        FaceDown
    }
    [Tooltip("e.g. Landscape, Portrait, etc.")]
    public Orientation DeviceOrientation;
    public enum Tool {
        Touch,
        Pen
    }
    public Tool ToolType;

    // Events
    MultiDeviceEventManager eventManager;
    public enum EventType {
        TouchDown,
        TouchUp,
        TouchMove,
        Tap,
        TapConfirmed,
        DoubleTap,
        LongPress,
        PinchStart,
        PinchMove,
        PinchEnd,
        Fling
    }
    public struct DeviceEvent {
        public EventType eventType;
        public int touchID;
        public object data;
    }
    Queue<DeviceEvent> eventQueue;

    // Communication
    Transceiver transceiver;
    

    // Start is called before the first frame update
    public virtual void Start()
    {
        // initialize lists
        TouchPos = new Vector2[maxNumberOfTouchIDs];
        TouchDelta = new Vector2[maxNumberOfTouchIDs];
        TouchSize = new float[maxNumberOfTouchIDs];
        isTouched = new bool[maxNumberOfTouchIDs];

        // initialize event manager
        eventManager = FindObjectOfType<MultiDeviceEventManager>();
        if (eventManager == null) Debug.LogError("MultiDeviceEventManager missing in scene"); 
        eventQueue = new Queue<DeviceEvent>();

        // initialize transciever
        transceiver = FindObjectOfType<Transceiver>();
        if (transceiver == null) Debug.LogError("Transceiver missing in scene"); 
        eventQueue = new Queue<DeviceEvent>();
    }

    // Update is called once per frame
    public virtual void Update()
    {
        // handle events in queue
        while (eventQueue.Count > 0) {
            HandleEvent(eventQueue.Dequeue());
        }

        // update gameobject name to reflect Device ID
        if (ID >= 0) {
            gameObject.name = "Device " + ID;
        }
    }

    public virtual void ParseData(string message) {
        //PrintMessage(message);
        ParseStandardData(message);
    }

    private void HandleEvent(DeviceEvent deviceEvent) {

        switch (deviceEvent.eventType) {
            case EventType.TouchDown:
                eventManager.TouchDown(ID, deviceEvent.touchID, (Vector2) deviceEvent.data);
                break;
            
            case EventType.TouchUp:
                eventManager.TouchUp(ID, deviceEvent.touchID, (Vector2) deviceEvent.data);
                break;

            case EventType.TouchMove:
                eventManager.TouchMove(ID, deviceEvent.touchID, (Vector2) deviceEvent.data);
                break;

            case EventType.Tap:
                eventManager.Tap(ID, deviceEvent.touchID, (Vector2) deviceEvent.data, TapCount);
                break;

            case EventType.TapConfirmed:
                eventManager.TapConfirmed(ID, deviceEvent.touchID, (Vector2) deviceEvent.data);
                break;

            case EventType.DoubleTap:
                eventManager.DoubleTap(ID, deviceEvent.touchID, (Vector2) deviceEvent.data, TapCount);
                break;

            case EventType.LongPress:
                eventManager.LongPress(ID, deviceEvent.touchID, (Vector2) deviceEvent.data);
                break;

            case EventType.PinchStart:
                eventManager.PinchStart(ID, (float) deviceEvent.data);
                break;

            case EventType.PinchEnd:
                eventManager.PinchEnd(ID, (float) deviceEvent.data);
                break;

            case EventType.PinchMove:
                eventManager.PinchMove(ID, (float) deviceEvent.data);
                break;

            case EventType.Fling:
                eventManager.Fling(ID, (Vector2) deviceEvent.data);
                break;

            default:
                Debug.LogError("Unrecognized device event");
                break;
        }
    }

    public void ParseStandardData(string message) {
        string[] strings = message.Split(',');

        long timeStamp = long.Parse(strings[0]);
        string[] data = strings[1..strings.Length];

        string header = data[0];
        int touchID;
        DeviceEvent deviceEvent = new DeviceEvent();

        switch (header)
        {
            #region RAWTOUCH
            case "TOUCH_DOWN":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                isTouched[touchID] = true;
                SaveTouchInfo(data);

                // queue event
                deviceEvent.eventType = EventType.TouchDown;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;

            case "TOUCH_UP":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                isTouched[touchID] = false;
                SaveTouchInfo(data);
                
                // queue event
                deviceEvent.eventType = EventType.TouchUp;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;

            case "TOUCH_MOVE":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                isTouched[touchID] = true;
                SaveTouchInfo(data);
                
                // queue event
                deviceEvent.eventType = EventType.TouchMove;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchDelta[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;
            #endregion

            #region TOUCHGESTURES
            case "TAP":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                TapCount = int.Parse(data[2]);
                
                // queue event
                deviceEvent.eventType = EventType.Tap;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;
            
            case "TAPCONFIRMED":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                TapCount = int.Parse(data[2]);
                
                // queue event
                deviceEvent.eventType = EventType.TapConfirmed;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;
            
            case "DOUBLETAP":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                TapCount = int.Parse(data[2]);
                
                // queue event
                deviceEvent.eventType = EventType.DoubleTap;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;

            case "LONGPRESS":
                touchID = int.Parse(data[1]);
                if (touchID >= maxNumberOfTouchIDs) return;
                
                // queue event
                deviceEvent.eventType = EventType.LongPress;
                deviceEvent.touchID = touchID;
                deviceEvent.data = TouchPos[touchID];
                eventQueue.Enqueue(deviceEvent);
                break;

            case "FLING":
                Vector2 flingDir = new Vector2(float.Parse(data[1]), 
                                               float.Parse(data[2]));

                // queue event
                deviceEvent.eventType = EventType.Fling;
                deviceEvent.data = flingDir;
                eventQueue.Enqueue(deviceEvent);
                break;
            
            case "PINCH_START":
                PinchSpan = float.Parse(data[1]);

                // queue event
                deviceEvent.eventType = EventType.PinchStart;
                deviceEvent.data = PinchSpan;
                eventQueue.Enqueue(deviceEvent);
                break;

            case "PINCH_MOVE":
                PinchSpan = float.Parse(data[1]);
                
                // queue event
                deviceEvent.eventType = EventType.PinchMove;
                deviceEvent.data = PinchSpan;
                eventQueue.Enqueue(deviceEvent);
                break;

            case "PINCH_END":
                PinchSpan = float.Parse(data[1]);

                // queue event
                deviceEvent.eventType = EventType.PinchEnd;
                deviceEvent.data = PinchSpan;
                eventQueue.Enqueue(deviceEvent);
                break;
            #endregion
            
            #region SENSORS
            case "ARPOSE":
                // save x y z position
                PositionAR = new Vector3(float.Parse(data[1]), 
                                    float.Parse(data[2]), 
                                    float.Parse(data[3]));
                PositionAR = convertAndroidToUnityVector3(PositionAR);

                // save orientation
                RotationAR = new Quaternion(float.Parse(data[4]), 
                                          float.Parse(data[5]), 
                                          float.Parse(data[6]),
                                          float.Parse(data[7]));
                RotationAR = convertAndroidToUnityQuaternion(RotationAR, header);
                break;

            case "ACCELEROMETER":
                // save x y z acceleration (including gravity)
                Accelerometer = new Vector3(float.Parse(data[1]), 
                                    float.Parse(data[2]), 
                                    float.Parse(data[3]));
                break;

            case "LINEAR_ACCELERATION":
                // save x y z linear acceleration
                LinearAcceleration = new Vector3(float.Parse(data[1]), 
                                                 float.Parse(data[2]), 
                                                 float.Parse(data[3]));
                break;

            case "GRAVITY":
                // save x y z force of gravity
                Gravity = new Vector3(float.Parse(data[1]), 
                                                 float.Parse(data[2]), 
                                                 float.Parse(data[3]));
                break;

            case "GAME_ROTATION_VECTOR":
                // save x y z w gyroscope orientation
                var GameRotVector_Android = new Quaternion(float.Parse(data[1]), 
                                            float.Parse(data[2]), 
                                            float.Parse(data[3]),
                                            float.Parse(data[4]));
                GameRotationVector = convertAndroidToUnityQuaternion(GameRotVector_Android, header);
                break;

            case "ROTATION_VECTOR":
                // save x y z w gyroscope orientation
                var RotVector_Android = new Quaternion(float.Parse(data[1]), 
                                            float.Parse(data[2]), 
                                            float.Parse(data[3]),
                                            float.Parse(data[4]));
                RotationVector = convertAndroidToUnityQuaternion(RotVector_Android, header);
                break;
            
            case "MAGNETIC_FIELD":
                // save x y z magnetic field
                MagneticField = new Vector3(float.Parse(data[1]), 
                                            float.Parse(data[2]), 
                                            float.Parse(data[3]));
                break;

            case "GYROSCOPE":
                // save x y z gyroscope reading
                Gyroscope = new Vector3(float.Parse(data[1]), 
                                            float.Parse(data[2]), 
                                            float.Parse(data[3]));
                break;

            case "PROXIMITY":
                // save proximity reading
                Proximity = float.Parse(data[1]);
                break;

            case "AMBIENT_TEMPERATURE":
                // save ambient temperature reading
                AmbientTemperature = float.Parse(data[1]);
                break;

            case "LIGHT":
                // save light reading
                Light = float.Parse(data[1]);
                break;

            case "DEVICE_ORIENTATION":
                // save orientation
                string msg = data[1];
                switch (msg) {
                    case "PORTRAIT":
                        DeviceOrientation = Orientation.Portrait;
                        break;
                    case "LANDSCAPE_LEFT":
                        DeviceOrientation = Orientation.LandscapeLeft;
                        break;
                    case "LANDSCAPE_RIGHT":
                        DeviceOrientation = Orientation.LandscapeRight;
                        break;
                    case "PORTRAIT_UPSIDE_DOWN":
                        DeviceOrientation = Orientation.PortraitUpsideDown;
                        break;
                    case "FACE_UP":
                        DeviceOrientation = Orientation.FaceUp;
                        break;
                    case "FACE_DOWN":
                        DeviceOrientation = Orientation.FaceDown;
                        break;
                }
                break;

            case "DEVICE_INFO":
                receivedDeviceInfo = true;
                DeviceName = data[1];
                Size_px = new Vector2(float.Parse(data[2]), 
                                      float.Parse(data[3]));
                var Size_in = new Vector2(float.Parse(data[4]), 
                                          float.Parse(data[5]));
                
                Size_m = Size_in * 0.0254f;
                break;

            default:
                Debug.Log("[Device " + ID.ToString() + "] Received message with unknown header: " + header);
                break;
            #endregion
        }
    }

    public virtual void PrintMessage(string message) {
        Debug.Log("[Device " + ID.ToString() + "] Received message: " + message);
    }

    private void SaveTouchInfo(string[] data) {
        // get touch ID
        var touchID = int.Parse(data[1]);

        // if touchID is too large, break
        if (touchID >= maxNumberOfTouchIDs) {
            return;
        }

        // save positions
        TouchPos[touchID] = new Vector2(float.Parse(data[2]), 
                                        float.Parse(data[3]));

        // save size and pressure
        TouchSize[touchID] = float.Parse(data[4]);
        Pressure = float.Parse(data[5]);

        // save deltas
        TouchDelta[touchID] = new Vector2(float.Parse(data[6]), 
                                        float.Parse(data[7]));

        // save tool (cast to ToolType based on enum)
        // 0 is Touch
        // 1 is Pen
        ToolType = (Tool) (int.Parse(data[8]) - 1);
    }

    private Quaternion convertAndroidToUnityQuaternion(Quaternion androidQuat, string sensor = null) {
        // baseline remapping to left handed coordinate system
        // (x right, y up, z towards user) ---> (x right, y up, z away from user)
        Quaternion unityQuat = new Quaternion(androidQuat.x, 
                                   -1f * androidQuat.y, 
                                   -1f * androidQuat.z, 
                                   androidQuat.w);

        // to align android sensor (not ARcore) coordinates with ARcore (y-up), need additional -90 deg rotation about x
        if (sensor == "GAME_ROTATION_VECTOR") unityQuat = Quaternion.AngleAxis(-90f,Vector3.right) * unityQuat;
        if (sensor == "ROTATION_VECTOR") unityQuat = Quaternion.AngleAxis(-90f,Vector3.right) * unityQuat;

        return unityQuat;
    }

    private Vector3 convertAndroidToUnityVector3(Vector3 androidVec) {
        // remapping to left handed coordinate system
        // (x right, y up, z towards user) ---> (x right, y up, z away from user)
        Vector3 unityVec = new Vector3(androidVec.x, 
                                       androidVec.y, 
                                       -1f * androidVec.z);
        return unityVec;
    }

}

