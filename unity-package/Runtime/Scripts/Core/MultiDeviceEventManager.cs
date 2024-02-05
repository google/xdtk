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
using UnityEngine.Events;


public class MultiDeviceEventManager : MonoBehaviour
{
    #region RAWTOUCH
    public UnityEvent<int, int, Vector2> OnTouchDown;
    public void TouchDown(int deviceID, int touchID, Vector2 touchPos)
    {
        OnTouchDown.Invoke(deviceID, touchID, touchPos);
    }
    public UnityEvent<int, int, Vector2> OnTouchUp;
    public void TouchUp(int deviceID, int touchID, Vector2 touchPos)
    {
        OnTouchUp.Invoke(deviceID, touchID, touchPos);
    }
    public UnityEvent<int, int, Vector2> OnTouchMove;
    public void TouchMove(int deviceID, int touchID, Vector2 delta)
    {
        OnTouchMove.Invoke(deviceID, touchID, delta);
    }
    #endregion

    #region TOUCHGESTURES
    public UnityEvent<int, int, Vector2, int> OnTap;
    public void Tap(int deviceID, int touchID, Vector2 tapPos, int tapCount)
    {
        OnTap.Invoke(deviceID, touchID, tapPos, tapCount);
    }

    public UnityEvent<int, int, Vector2> OnTapConfirmed;
    public void TapConfirmed(int deviceID, int touchID, Vector2 tapPos)
    {
        OnTapConfirmed.Invoke(deviceID, touchID, tapPos);
    }

    public UnityEvent<int, int, Vector2, int> OnDoubleTap;
    public void DoubleTap(int deviceID, int touchID, Vector2 tapPos, int tapCount)
    {
        OnDoubleTap.Invoke(deviceID, touchID, tapPos, tapCount);
    }

    public UnityEvent<int, int, Vector2> OnLongPress;
    public void LongPress(int deviceID, int touchID, Vector2 tapPos)
    {
        OnLongPress.Invoke(deviceID, touchID, tapPos);
    }

    public UnityEvent<int, float> OnPinchStart;
       public void PinchStart(int deviceID, float span) {
        OnPinchStart.Invoke(deviceID, span);
    }

    public UnityEvent<int, float> OnPinchEnd;
    public void PinchEnd(int deviceID, float span) {
        OnPinchEnd.Invoke(deviceID, span);
    }

    public UnityEvent<int, float> OnPinchMove;
    public void PinchMove(int deviceID, float span) {
        OnPinchMove.Invoke(deviceID, span);
    }

    public UnityEvent<int, Vector2> OnFling;
    public void Fling(int deviceID, Vector2 direction) {
        OnFling.Invoke(deviceID, direction);
    }

    #endregion
}
