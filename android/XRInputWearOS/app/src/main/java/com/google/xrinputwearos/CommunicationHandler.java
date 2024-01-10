/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.xrinputwearos;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Sensor;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.ScaleGestureDetector;
// import com.google.ar.core.Pose;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CommunicationHandler {
  private Transceiver transceiver;
  private final int sendPort = 5555;
  private final int receivePort = 5556;
  private Boolean isConnected = false;
  private Activity mainApp;

  // timer
  private Timer resetHeartbeatTimer;
  private TimerTask resetHeartbeatTask;
  private boolean timerRunning = false;
  private final long heartbeatThreshold_ms = 1000;
  private long timeOfLastMsg_touchMove_ms = 0;
  private final long msgRate_touchMove_ms = 50;
  private long timeOfLastMsg_DeviceInfo_ms = 0;
  private final long msgRate_DeviceInfo_ms = 20;

  public CommunicationHandler(Activity activity) {
    mainApp = activity;
    resetHeartbeatTimer = new Timer();
    initResetHeartbeatTask();
  }

  public void openConnection(String ipAddress) {
    transceiver = new Transceiver(ipAddress, sendPort, receivePort, this);
  }

  public void closeConnection() {
    transceiver.close();
  }

  private void initResetHeartbeatTask() {
    resetHeartbeatTask =
        new TimerTask() {
          @Override
          public void run() {
            // reset heartbeat
            isConnected = false;
          }
        };
  }

  /** Getter Functions */
  public boolean isRunning() {
    if (transceiver == null) {
      return false;
    } else {
      return transceiver.isRunning();
    }
  }

  public boolean isConnected() {
    return isConnected;
  }

  /** Sensor Messages */
  public void sendDeviceOrientation(SensorHandler sensorHandler) {
    String msg = "DEVICE_ORIENTATION," + sensorHandler.getDeviceOrientation();
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendAccelerometer(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_ACCELEROMETER);
    if (val == null) return;

    String msg = "ACCELEROMETER," + val[0] + "," + val[1] + "," + val[2];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendLinearAcceleration(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_LINEAR_ACCELERATION);
    if (val == null) return;

    String msg = "LINEAR_ACCELERATION," + val[0] + "," + val[1] + "," + val[2];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendGravity(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_GRAVITY);
    if (val == null) return;

    String msg = "GRAVITY," + val[0] + "," + val[1] + "," + val[2];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendGyroscope(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_GYROSCOPE);
    if (val == null) return;

    String msg = "GYROSCOPE," + val[0] + "," + val[1] + "," + val[2];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendGameRotationVector(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_GAME_ROTATION_VECTOR);
    if (val == null) return;

    String msg = "GAME_ROTATION_VECTOR," + val[0] + "," + val[1] + "," + val[2] + "," + val[3];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendRotationVector(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_ROTATION_VECTOR);
    if (val == null) return;

    String msg = "ROTATION_VECTOR," + val[0] + "," + val[1] + "," + val[2] + "," + val[3];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendMagneticField(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_MAGNETIC_FIELD);
    if (val == null) return;

    String msg = "MAGNETIC_FIELD," + val[0] + "," + val[1] + "," + val[2];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendProximity(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_PROXIMITY);
    if (val == null) return;

    String msg = "PROXIMITY," + val[0];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendAmbientTemperature(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_AMBIENT_TEMPERATURE);
    if (val == null) return;

    String msg = "AMBIENT_TEMPERATURE," + val[0];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendLight(SensorHandler sensorHandler) {
    float[] val = sensorHandler.getSensorValues(Sensor.TYPE_LIGHT);
    if (val == null) return;

    String msg = "LIGHT," + val[0];
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  /** ARCore Pose Messages */
  // public void sendPose(Pose pose) {
  //   float[] position = pose.getTranslation();         // x y z
  //   float[] rotation = pose.getRotationQuaternion();  // x y z w
  //   String msg =
  //       "ARPOSE," + position[0] + "," + position[1] + "," + position[2] + "," +
  //           rotation[0] + "," + rotation[1] + "," + rotation[2] + "," + rotation[3];
  //   if (transceiver != null) {
  //     transceiver.sendData(msg);
  //   }
  // }

  /** Touch Messages */
  public void sendTouchDown(Touch touch) {
    String msg =
        "TOUCH_DOWN,"
            + touch.ID
            + ","
            + touch.positionX
            + ","
            + touch.positionY
            + ","
            + touch.size
            + ","
            + touch.pressure
            + ","
            + touch.deltaX
            + ","
            + touch.deltaY
            + ","
            + touch.toolType;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendTouchUp(Touch touch) {
    String msg =
        "TOUCH_UP,"
            + touch.ID
            + ","
            + touch.positionX
            + ","
            + touch.positionY
            + ","
            + touch.size
            + ","
            + touch.pressure
            + ","
            + touch.deltaX
            + ","
            + touch.deltaY
            + ","
            + touch.toolType;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendTouchMove(Touch touch) {
    if (System.currentTimeMillis() - timeOfLastMsg_touchMove_ms > msgRate_touchMove_ms) {
      timeOfLastMsg_touchMove_ms = System.currentTimeMillis();
      String msg =
          "TOUCH_MOVE,"
              + touch.ID
              + ","
              + touch.positionX
              + ","
              + touch.positionY
              + ","
              + touch.size
              + ","
              + touch.pressure
              + ","
              + touch.deltaX
              + ","
              + touch.deltaY
              + ","
              + touch.toolType;
      if (transceiver != null) {
        transceiver.sendData(msg);
      }
    }
  }

  public void sendTap(int pointerID, int tapCount) {
    String msg = "TAP," + pointerID + "," + tapCount;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendTapConfirmed(int pointerID, int tapCount) {
    String msg = "TAPCONFIRMED," + pointerID + "," + tapCount;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendDoubleTap(int pointerID, int tapCount) {
    String msg = "DOUBLETAP," + pointerID + "," + tapCount;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendLongPress(int pointerID) {
    String msg = "LONGPRESS," + pointerID;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendFling(int pointerID, float velocityX, float velocityY) {
    String msg = "FLING," + velocityX + "," + velocityY;
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendPinchStart(ScaleGestureDetector detector) {
    String msg = "PINCH_START," + detector.getCurrentSpan();
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendPinch(ScaleGestureDetector detector) {
    String msg = "PINCH_MOVE," + detector.getCurrentSpan();
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  public void sendPinchEnd(ScaleGestureDetector detector) {
    String msg = "PINCH_END," + detector.getCurrentSpan();
    if (transceiver != null) {
      transceiver.sendData(msg);
    }
  }

  /** Device Information Messages */
  public void sendDeviceInfo() {

    if (System.currentTimeMillis() - timeOfLastMsg_DeviceInfo_ms > msgRate_DeviceInfo_ms) {
      timeOfLastMsg_DeviceInfo_ms = System.currentTimeMillis();

      // Get device model
      String modelName = Build.MODEL;
      String manufacturer = Build.MANUFACTURER;
      String deviceName =
          manufacturer.substring(0, 1).toUpperCase(Locale.ROOT)
              + manufacturer.substring(1)
              + " "
              + modelName;

      // Get display size
      DisplayMetrics displayMetrics = mainApp.getResources().getDisplayMetrics();
      Point point = new Point();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        mainApp.getWindowManager().getDefaultDisplay().getRealSize(point);
      } else {
        mainApp.getWindowManager().getDefaultDisplay().getSize(point);
      }
      float widthPx = point.x;
      float heightPx = point.y;
      float widthInches = widthPx / displayMetrics.xdpi;
      float heightInches = heightPx / displayMetrics.ydpi;

      String msg =
          "DEVICE_INFO,"
              + deviceName
              + ","
              + widthPx
              + ","
              + heightPx
              + ","
              + widthInches
              + ","
              + heightInches;
      if (transceiver != null) {
        transceiver.sendData(msg);
      }
    }
  }

  /** Receive Messages */
  public void parseReceivedMessage(String message) {
    String[] messageParts = message.split(",");

    // Ensure there's at least one element in the array
    if (messageParts.length > 0) {
      String header = messageParts[0];

      switch (header) {
        case "HEARTBEAT":
          // update connection bool
          isConnected = true;

          // restart tap timer
          if (timerRunning) {
            // cancel previous timer
            resetHeartbeatTask.cancel();
            resetHeartbeatTimer.purge();
          }
          initResetHeartbeatTask();
          timerRunning = true;
          resetHeartbeatTimer.schedule(resetHeartbeatTask, heartbeatThreshold_ms);
          break;

        case "WHOAREYOU":
          sendDeviceInfo();
          break;
      }
    }
  }
}
