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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/** This application streams sensor data over a specified wireless network. */
public class MainActivity extends WearableActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  // Display Variables
  private TextView deviceIPText;
  private EditText hmdIPText;
  private Button connectButton;
  private String hmdIPstring = "192.168.0.1";
  private GradientDrawable connectionIndicator;

  // Thread
  private Handler handler = new Handler();
  private Runnable runnableCode;

  // Handlers
  private SensorHandler sensorHandler;
  private TouchHandler touchHandler;
  private CommunicationHandler communicationHandler = new CommunicationHandler(MainActivity.this);
  private boolean sendingDataFlag = false;
  private int tapsToStopConnection = 8;
  private int tapsRemainingToStopConnection = tapsToStopConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Keep device screen on
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    // Initialize TextViews
    deviceIPText = findViewById((R.id.deviceip_text));

    // Initialize UI
    initUI();

    // Initialize handlers
    initSensorHandler();
    initTouchHandler();

    // Define the code block to be executed
    runnableCode =
        new Runnable() {
          @Override
          public void run() {
            updateDisplayInfo();

            // Polling-based Communication
            if (communicationHandler.isRunning()) {

              // Sensors
              communicationHandler.sendAccelerometer(sensorHandler);
              communicationHandler.sendGravity(sensorHandler);
              communicationHandler.sendGyroscope(sensorHandler);
              communicationHandler.sendLinearAcceleration(sensorHandler);
              communicationHandler.sendRotationVector(sensorHandler);
              communicationHandler.sendGameRotationVector(sensorHandler);
              communicationHandler.sendMagneticField(sensorHandler);
              communicationHandler.sendProximity(sensorHandler);
              communicationHandler.sendAmbientTemperature(sensorHandler);
              communicationHandler.sendLight(sensorHandler);
              communicationHandler.sendDeviceOrientation(sensorHandler);

              // Check if we should stop communication
              if (tapsRemainingToStopConnection == 0) {
                sendingDataFlag = false;
                communicationHandler.closeConnection();

                connectButton.setText(R.string.connect_text);
                connectButton.setClickable(true);
                connectButton.setEnabled(true);
                hmdIPText.setClickable(true);
                hmdIPText.setEnabled(true);
              }
            }

            // Repeat this runnable code block again every 80 ms
            // note: reduced communication rate for WearOS (c.f. 10 ms for standard Android)
            handler.postDelayed(runnableCode, 80);
          }
        };

    // Start the initial runnable task by posting through the handler
    handler.post(runnableCode);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    sensorHandler.resumeAllSensorListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    sensorHandler.pauseAllSensorListeners();
  }

  /** Initialization Functions */
  private void initSensorHandler() {
    sensorHandler = new SensorHandler(this);

    // Motion Sensors
    // https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion
    sensorHandler.registerSensorListener(Sensor.TYPE_ACCELEROMETER);
    sensorHandler.registerSensorListener(Sensor.TYPE_GRAVITY);
    sensorHandler.registerSensorListener(Sensor.TYPE_GYROSCOPE);
    sensorHandler.registerSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
    sensorHandler.registerSensorListener(Sensor.TYPE_ROTATION_VECTOR);

    // Position Sensors
    // https://developer.android.com/develop/sensors-and-location/sensors/sensors_position
    sensorHandler.registerSensorListener(Sensor.TYPE_GAME_ROTATION_VECTOR);
    sensorHandler.registerSensorListener(Sensor.TYPE_MAGNETIC_FIELD);
    sensorHandler.registerSensorListener(Sensor.TYPE_PROXIMITY);

    // Environment Sensors
    // https://developer.android.com/develop/sensors-and-location/sensors/sensors_environment
    sensorHandler.registerSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
    sensorHandler.registerSensorListener(Sensor.TYPE_LIGHT);
  }

  private void initTouchHandler() {
    touchHandler = new TouchHandler(this, communicationHandler);

    // we created a view that lives in front of the display to capture all touch events
    View rootView = findViewById(R.id.touchview);
    rootView.setOnTouchListener(touchHandler);

    // this makes sure that this view lies behind everything and doesn't consume
    // touch events for buttons etc.
    rootView.setTranslationZ(-100f);
  }

  private void initUI() {
    // Initialize HMD IP editor
    hmdIPText = findViewById((R.id.hmdip_text));
    hmdIPText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              // User pressed Done button, do something
              InputMethodManager imm =
                  (InputMethodManager)
                      v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

              // only allow '0-9' and '.'
              hmdIPstring = v.getText().toString().trim().replaceAll("[^0-9.]", "");

              // save string to memory
              SharedPreferences sharedPref =
                  getSharedPreferences("DeviceInputXRPreferences", MODE_PRIVATE);
              SharedPreferences.Editor editor = sharedPref.edit();
              editor.putString("hmdIPstring", hmdIPstring);
              editor.apply();

              // set displayed text
              hmdIPText.setText(hmdIPstring);
              return true;
            }
            return false;
          }
        });

    // Load last HMD IP Address from memory
    SharedPreferences sharedPref = getSharedPreferences("DeviceInputXRPreferences", MODE_PRIVATE);
    hmdIPstring = sharedPref.getString("hmdIPstring", hmdIPstring);
    if (hmdIPstring != null) hmdIPText.setText(hmdIPstring);

    // Initialize buttons
    connectButton = findViewById(R.id.connect_button);
    connectButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            // This code will be executed when the button is pressed
            if (!sendingDataFlag) {
              sendingDataFlag = true;
              communicationHandler.openConnection(hmdIPstring);
              Log.d(TAG, "Started sending data");

              // disable button
              connectButton.setClickable(false);
              connectButton.setEnabled(false);
              hmdIPText.setClickable(false);
              hmdIPText.setEnabled(false);
            }
          }
        });

    // Initialize connection status indicator
    // > create an instance of GradientDrawable and set its shape to be an oval
    connectionIndicator = new GradientDrawable();
    connectionIndicator.setShape(GradientDrawable.OVAL);
    View view = findViewById(R.id.colorview);

    // > set the gradient radius and type
    connectionIndicator.setGradientRadius(Math.min(view.getWidth(), view.getHeight()) / 2f);
    connectionIndicator.setGradientType(GradientDrawable.RADIAL_GRADIENT);

    // > set the gradient colors
    connectionIndicator.setColor(Color.parseColor("#b5b5b5"));

    // > set the drawable as the background of the view
    view.setBackground(connectionIndicator);
  }

  /** Display functions */
  @SuppressLint({"DefaultLocale", "SetTextI18n"})
  private void updateDisplayInfo() {

    // ------ CONNECTION --------
    if (communicationHandler.isRunning() && !communicationHandler.isConnected()) {
      // connectionStatusText.setText("sending...");
      connectionIndicator.setColor(Color.parseColor("#ffb13d"));
    } else if (communicationHandler.isRunning() && communicationHandler.isConnected()) {
      // connectionStatusText.setText("connected");
      connectionIndicator.setColor(Color.parseColor("#59c639"));
    } else {
      // connectionStatusText.setText("not connected");
      connectionIndicator.setColor(Color.parseColor("#b5b5b5"));
    }
    deviceIPText.setText(getLocalIpAddress());

    if (sendingDataFlag) {
      tapsRemainingToStopConnection = tapsToStopConnection - touchHandler.getCurrentTapCount();
      connectButton.setText(tapsRemainingToStopConnection + " taps to disconnect");
    }
  }

  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
          en.hasMoreElements(); ) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
            enumIpAddr.hasMoreElements(); ) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
            return inetAddress.getHostAddress();
          }
        }
      }
    } catch (SocketException ex) {
      Log.e("getLocalIpAddress", ex.toString());
    }
    Log.d(TAG, "No IPv4 address found");
    return "No IPv4 address found.";
  }
}
