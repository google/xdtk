# XDTK: Android App & Communication

![System](../media/system.png)

## Overview
The Android app streams data from built-in sensors, touch events, and (when applicable) ARCore at 100 Hz from Phone/Tablet and 12.5 Hz from Watch using UDP. In general, message headers for sensors follow the naming convention used by [Android](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview).

### Data Structure
Datagrams are formed as comma separated string, encoded into byte array using UTF-8. Packets consist of a timestamp (ms), message header, and data values. For example `"HEADER, value1, value2, value3"`, which we write here as:

|        |        |        |        |
|:------:|:------:|:------:|:------:|
| Header | value1 | value2 | value3 |


#### Raw Touch Events
Refer to Android reference [here](https://developer.android.com/develop/ui/views/touch-and-input/gestures/detector).

|                |         |           |           |           |               |        |        |          |
|:--------------:|:-------:|:---------:|:---------:|-----------|---------------|--------|--------|----------|
| **TOUCH_DOWN** | touchID | positionX | positionY | touchSize | touchPressure | deltaX | deltaY | toolType |
| **TOUCH_MOVE** | touchID | positionX | positionY | touchSize | touchPressure | deltaX | deltaY | toolType |
|  **TOUCH_UP**  | touchID | positionX | positionY | touchSize | touchPressure | deltaX | deltaY | toolType |

#### Touch Gestures
Refer to Android reference [here](https://developer.android.com/develop/ui/views/touch-and-input/gestures/detector).

|                  |           |           |
|------------------|-----------|-----------|
|      **TAP**     |  touchID  |  tapCount |
| **TAPCONFIRMED** |  touchID  |  tapCount |
|   **DOUBLETAP**  |  touchID  |  tapCount |
|     **FLING**    | velocityX | velocityY |
|  **PINCH_START** |    span   |           |
|  **PINCH_MOVE**  |    span   |           |
|  **PINCH_START** |    span   |           |
|   **LONGPRESS**  |  touchID  |           |

#### Sensors
Refer to Android reference [here](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview).

|                          |       |   |   |   |
|:------------------------:|:-----:|:-:|:-:|:-:|
|     **ACCELEROMETER**    |   x   | y | z |   |
|  **LINEAR_ACCELERATION** |   x   | y | z |   |
|        **GRAVITY**       |   x   | y | z |   |
|       **GYROSCOPE**      |   x   | y | z |   |
|    **ROTATION_VECTOR**   |   x   | y | z | w |
| **GAME_ROTATION_VECTOR** |   x   | y | z | w |
|    **MAGNETIC_FIELD**    |   x   | y | z |   |
|       **PROXIMITY**      |  prox |   |   |   |
|  **AMBIENT_TEMPERATURE** |  temp |   |   |   |
|         **LIGHT**        | light |   |   |   |
|  **DEVICE_ORIENTATION**  | pose\* |   |   |   |

\*Values for pose:
* `PORTRAIT`
* `LANDSCAPE_LEFT`
* `LANDSCAPE_RIGHT`
* `PORTRAIT_UPSIDE_DOWN`
* `FACE_UP`
* `FACE_DOWN`

#### ARCore
Refer to ARCore reference [here](https://developers.google.com/ar/reference/java/com/google/ar/core/Pose). This message combines [translation vector](https://developers.google.com/ar/reference/java/com/google/ar/core/Pose#getTranslation()) and [rotation quaternion](https://developers.google.com/ar/reference/java/com/google/ar/core/Pose#getRotationQuaternion(float[],%20int)).

|            |      |      |      |      |      |      |      |
|:----------:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|
| **ARPOSE** | posX | posY | posZ | rotX | rotY | rotZ | rotW |


#### Device Info
The Android device provides its name and screen size whenever it receives a `WHOAREYOU` message from Unity.
|                 |            |         |          |             |              |
|:---------------:|:----------:|:-------:|:--------:|:-----------:|:------------:|
| **DEVICE_INFO** | deviceName | widthPx | heightPx | widthInches | heightInches |


### Messages from Unity
Unity responds with a `HEARTBEAT` message everytime it reveives a message from an Android device. The Android device must receive a `HEARTBEAT` within 1 second of sending its message in order to maintain a `Connected` status in the app (i.e., green indicator).


