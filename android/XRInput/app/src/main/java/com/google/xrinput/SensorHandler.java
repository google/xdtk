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

package com.google.xrinput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class SensorHandler {
  private final String TAG = SensorHandler.class.getSimpleName();
  private SensorManager sensorManager;
  private Map<Integer, SensorEventListener> sensorEventListeners = new HashMap<>();
  private Map<Integer, float[]> sensorValues = new HashMap<>();
  private String deviceOrientation;

  public SensorHandler(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    // ensure gravity sensor is registered for device orientation
    registerSensorListener(Sensor.TYPE_GRAVITY);
  }

  public void registerSensorListener(int sensorType) {
    Sensor sensor = sensorManager.getDefaultSensor(sensorType);

    if (sensor != null) {
      // set sampling rate
      sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

      // store listener
      sensorEventListeners.put(sensorType, listener);
    } else {
      Log.e("SensorHandler", "Sensor of type " + sensorType + " not available.");
    }
  }

  private SensorEventListener listener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {
      // update values for use later
      sensorValues.put(event.sensor.getType(), event.values.clone());

      // compute device orientation (e.g. portrait, landscape)
      if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
        float[] gravity = new float[3];
        gravity[0] = event.values[0];
        gravity[1] = event.values[1];
        gravity[2] = event.values[2];

        double gravityNorm =
            Math.sqrt(
                gravity[0] * gravity[0]
                    + gravity[1] * gravity[1]
                    + gravity[2] * gravity[2]);

        // Normalize gravity vector.
        gravity[0] /= gravityNorm;
        gravity[1] /= gravityNorm;
        gravity[2] /= gravityNorm;

        // set device orientation based on gravity direction
        if (gravity[2] > 0.8) {
          deviceOrientation = "FACE_UP";
        } else if (gravity[2] < -0.8) {
          deviceOrientation = "FACE_DOWN";
        } else if (gravity[0] > 0.8) {
          deviceOrientation = "LANDSCAPE_LEFT";
        } else if (gravity[0] < -0.8) {
          deviceOrientation = "LANDSCAPE_RIGHT";
        } else if (gravity[1] > 0.8) {
          deviceOrientation = "PORTRAIT";
        } else if (gravity[1] < -0.8) {
          deviceOrientation = "PORTRAIT_UPSIDE_DOWN";
        } else {
          // deviceOrientation = "UNKNOWN";
        }
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // Handle accuracy changes
    }
  };

  public void unregisterSensorListener(int sensorType) {
    SensorEventListener listener = sensorEventListeners.get(sensorType);
    if (listener != null) {
      sensorManager.unregisterListener(listener);
      sensorEventListeners.remove(sensorType);
    }
  }

  public void removeAllSensorListeners() {
    for (Map.Entry<Integer, SensorEventListener> entry : sensorEventListeners.entrySet()) {
      sensorManager.unregisterListener(entry.getValue());
    }
    sensorEventListeners.clear(); // remove all entries from the map
  }

  public void pauseAllSensorListeners() {
    for (Map.Entry<Integer, SensorEventListener> entry : sensorEventListeners.entrySet()) {
      sensorManager.unregisterListener(entry.getValue());
    }
  }

  public void resumeAllSensorListeners() {
    for (Map.Entry<Integer, SensorEventListener> entry : sensorEventListeners.entrySet()) {
      registerSensorListener(entry.getKey());
    }
  }

  public float[] getSensorValues(int sensorType) {
    return sensorValues.get(sensorType);
  }

  @SuppressLint("DefaultLocale")
  public String getStringOfSensorsAndValues() {
    StringBuilder result = new StringBuilder();

    // loop through all sensors
    for (Map.Entry<Integer, SensorEventListener> entry : sensorEventListeners.entrySet()) {
      Sensor sensor = sensorManager.getDefaultSensor(entry.getKey());
      float[] values = sensorValues.get(entry.getKey());
      result.append(sensor.getName()).append("\n");
      if (values != null) {
        for (float value : values) {
          result.append(String.format("%.2f", value)).append(" ");
        }
      }
      result.append("\n");
    }

    return result.toString();
  }

  public void printDeviceOrientation() {
    // DEBUG
    Log.d(TAG, deviceOrientation);
  }

  public String getDeviceOrientation() {
    return deviceOrientation;
  }
}
