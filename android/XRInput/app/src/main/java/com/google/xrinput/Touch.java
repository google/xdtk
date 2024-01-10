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

import android.util.Log;

public class Touch {
  private final String TAG = Touch.class.getSimpleName();
  public float positionX;
  public float positionY;
  public float deltaX;
  public float deltaY;
  public float pressure;
  public float size;
  public int toolType;
  public int ID;

  public Touch(int ID_, float x, float y, int toolType_) {
    this.ID = ID_;
    this.positionX = x;
    this.positionY = y;
    this.toolType = toolType_;
  }

  public void update(float x, float y, float p, float s) {
    deltaX = x - positionX;
    deltaY = y - positionY;
    positionX = x;
    positionY = y;
    pressure = p;
    size = s;
  }

  public void printTouchState() {
    Log.d(TAG, "--------Touch " + ID + "--------");
    Log.d(
        TAG,
        "Pos: ("
            + String.format("%d", (int) positionX)
            + ", "
            + String.format("%d", (int) positionY)
            + ")");
    Log.d(
        TAG,
        "Delta: ("
            + String.format("%d", (int) deltaX)
            + ", "
            + String.format("%d", (int) deltaY)
            + ")");
    Log.d(
        TAG,
        "Pressure: " + String.format("%.2f", pressure) + ", Size: " + String.format("%.2f", size));
    Log.d(TAG, "ToolType: " + toolType);
  }
}
