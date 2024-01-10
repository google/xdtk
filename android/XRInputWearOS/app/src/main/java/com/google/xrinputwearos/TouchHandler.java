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

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TouchHandler
    implements View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {
  private final String TAG = TouchHandler.class.getSimpleName();
  private CommunicationHandler communicationHandler;

  private HashMap<Integer, Touch> touches = new HashMap<>();
  private GestureDetector gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;
  private int numberOfTouches = 0;
  private int currentTapCount = 0;
  private View currentView;

  // timer
  private Timer resetTapCountTimer;
  private TimerTask resetTapCountTask;
  private boolean timerRunning = false;
  private long tapDetectThreshold_ms = 500;

  // Constructor
  public TouchHandler(Context context, CommunicationHandler comm) {
    gestureDetector = new GestureDetector(context, this);
    gestureDetector.setOnDoubleTapListener(this);
    scaleGestureDetector = new ScaleGestureDetector(context, this);
    communicationHandler = comm;

    resetTapCountTimer = new Timer();
    initNewTapCountTimerTask();
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    int index = event.getActionIndex();
    int pointerId = event.getPointerId(index);
    currentView = v;

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        // Touch newTouch = new Touch(event.getX(index), event.getY(index));
        // touches.put(pointerId, newTouch);
        // Log.d(TAG, "onTouch: DOWN");
        // Log.d(TAG, "Action Index: " + Integer.toString(event.getActionIndex()));
        // Log.d(TAG, "Pointer ID: " +
        // Integer.toString(event.getPointerId(event.getActionIndex())));
        // Log.d(TAG, "Pointer Count: " + Integer.toString(event.getPointerCount()));
        // Log.d(TAG, "Pressure" + Float.toString(event.getPressure()));
        // Log.d(TAG, "Tool Type: " + Integer.toString(event.getToolType(pointerId)));
        // Log.d(TAG, "Raw X: " + Float.toString(event.getRawX()));
        // Log.d(TAG, "Raw Y: " + Float.toString(event.getRawY()));
        // Log.d(TAG, "X: " + Float.toString(event.getX()));

        // PROCESS TOUCH
        Touch newTouch =
            new Touch(
                pointerId,
                event.getRawX(index),
                event.getRawY(index),
                event.getToolType(pointerId));
        touches.put(pointerId, newTouch);

        // POST TOUCH EVENT
        communicationHandler.sendTouchDown(newTouch);
        numberOfTouches++;
        break;

      case MotionEvent.ACTION_MOVE:
        // PROCESS TOUCH
        for (int i = 0; i < event.getPointerCount(); i++) {
          pointerId = event.getPointerId(i);
          Touch touch = touches.get(pointerId);
          if (touch != null) {
            touch.update(
                event.getRawX(i), event.getRawY(i), event.getPressure(i), event.getSize(i));

            // POST TOUCH EVENT
            communicationHandler.sendTouchMove(touch);
            // touch.printTouchState();
          }
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        // PROCESS TOUCH
        Touch touch = touches.get(pointerId);
        touches.get(pointerId);
        if (touch != null) {
          touch.update(
              event.getRawX(index),
              event.getRawY(index),
              event.getPressure(index),
              event.getSize(index));

          // POST TOUCH EVENT
          communicationHandler.sendTouchUp(touch);
        }
        Log.d(TAG, "onTouchUp");
        numberOfTouches--;
        break;
    }

    scaleGestureDetector.onTouchEvent(event);
    return gestureDetector.onTouchEvent(event);
  }

  @Override
  public boolean onDown(MotionEvent event) {
    // Log.d(TAG,"onDown: " + event.toString());
    return true;
  }

  @Override
  public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
    if (Math.abs(velocityX) > 2000f || Math.abs(velocityY) > 2000f) {
      Log.d(TAG, "Vel: (" + velocityX + ", " + velocityY + ")");

      // Post message
      int index = event1.getActionIndex();
      int pointerId = event1.getPointerId(index);
      communicationHandler.sendFling(pointerId, velocityX, velocityY);
    }
    return true;
  }

  @Override
  public void onLongPress(MotionEvent event) {
    // Log.d(TAG, "onLongPress: " + event.toString());
    currentView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

    // Post message
    int index = event.getActionIndex();
    int pointerId = event.getPointerId(index);
    communicationHandler.sendLongPress(pointerId);
  }

  @Override
  public boolean onScroll(
      MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
    // Log.d(TAG, "onScroll: " + event1.toString() + event2.toString());
    return true;
  }

  @Override
  public void onShowPress(MotionEvent event) {
    // Log.d(TAG, "onShowPress: " + event.toString());
  }

  @Override
  public boolean onSingleTapUp(MotionEvent event) {
    Log.d(TAG, "onSingleTapUp: " + event.toString());
    // Use this and onDoubleTap for multi-tap counter

    // restart tap timer
    if (timerRunning) {
      // cancel previous timer
      resetTapCountTask.cancel();
      resetTapCountTimer.purge();
    }
    initNewTapCountTimerTask();
    timerRunning = true;
    resetTapCountTimer.schedule(resetTapCountTask, tapDetectThreshold_ms);

    // increase tapcount
    currentTapCount++;

    // DEBUG
    // Log.d(TAG,"TapCount: " + currentTapCount);

    // Post message
    int index = event.getActionIndex();
    int pointerId = event.getPointerId(index);
    communicationHandler.sendTap(pointerId, currentTapCount);
    return true;
  }

  @Override
  public boolean onDoubleTap(MotionEvent event) {
    Log.d(TAG, "onDoubleTap: " + event.toString());
    // Use this and onDoubleTap for multi-tap counter

    // restart tap timer
    if (timerRunning) {
      // cancel previous timer
      resetTapCountTask.cancel();
      resetTapCountTimer.purge();
    }
    initNewTapCountTimerTask();
    timerRunning = true;
    resetTapCountTimer.schedule(resetTapCountTask, tapDetectThreshold_ms);

    // increase tapcount
    currentTapCount++;

    // DEBUG
    // Log.d(TAG,"TapCount: " + currentTapCount);

    // Post message
    int index = event.getActionIndex();
    int pointerId = event.getPointerId(index);
    communicationHandler.sendDoubleTap(pointerId, currentTapCount);
    return false;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent event) {
    // Log.d(TAG, "onDoubleTapEvent: " + event.toString());
    return true;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent event) {
    // Log.d(TAG, "onSingleTapConfirmed: " + event.toString());

    // Post message
    int index = event.getActionIndex();
    int pointerId = event.getPointerId(index);
    communicationHandler.sendTapConfirmed(pointerId, currentTapCount);
    return true;
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    // Log.d(TAG, "Scaling: " + detector.getCurrentSpan());
    // Log.d(TAG, "Delta: " + (detector.getCurrentSpan() - detector.getPreviousSpan()));
    communicationHandler.sendPinch(detector);
    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    // Use this to detect scale/zoom gesures
    // Log.d(TAG, "Pinch Started");
    // Post message
    communicationHandler.sendPinchStart(detector);
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    // Log.d(TAG, "Pinch Ended");
    // Post message
    communicationHandler.sendPinchEnd(detector);
  }

  private void initNewTapCountTimerTask() {
    resetTapCountTask =
        new TimerTask() {
          @Override
          public void run() {
            // reset current tap count
            currentTapCount = 0;
            timerRunning = false;
          }
        };
  }

  public int getCurrentTapCount() {
    return currentTapCount;
  }
}
