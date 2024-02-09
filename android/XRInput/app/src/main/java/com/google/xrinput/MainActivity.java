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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;
import com.google.ar.core.examples.java.common.samplerender.Framebuffer;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer;
import com.google.ar.core.examples.java.common.samplerender.arcore.PlaneRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This application streams sensor and ARCore data over a specified wireless network. ARCore
 * components adapted from hello_ar_java example:
 * https://github.com/google-ar/arcore-android-sdk/tree/master/samples/hello_ar_java
 */
public class MainActivity extends AppCompatActivity implements SampleRender.Renderer {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final Boolean USE_AR_CORE = true;

  // Main ARCore Variables (NOTE: more below)
  private Session session;
  private Pose pose;
  private Frame frame;

  // Display Variables
  private TextView connectionStatusText;
  private TextView deviceIPText;
  private TextView hmdIPText;
  private TextView positionText;
  private TextView orientationText;
  private Button connectButton;
  private Button editHMDaddressButton;
  private Switch toggleARCoreSwitch;
  private String hmdIPstring = "192.168.0.1";
  private GradientDrawable connectionIndicator;

  // Thread
  private Handler handler = new Handler();
  private Runnable runnableCode;

  // Handlers
  private SensorHandler sensorHandler;
  private TouchHandler touchHandler;
  private CommunicationHandler communicationHandler;
  private boolean sendingDataFlag = false;
  private int tapsToStopConnection = 8;
  private int tapsRemainingToStopConnection = tapsToStopConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ARCoreOnCreate();
    disableSystemGestures();

    // Initialize TextViews
    connectionStatusText = findViewById((R.id.status_text));
    deviceIPText = findViewById((R.id.deviceip_text));
    hmdIPText = findViewById((R.id.hmdip_text));
    positionText = findViewById((R.id.position_text));
    orientationText = findViewById((R.id.orientation_text));

    // Initialize UI
    initUI();

    // Initialize handlers
    initCommunicationHandler();
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

              // ARCore
              if (USE_AR_CORE) {
                if (frame != null
                    && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                  communicationHandler.sendPose(pose);
                }
              }

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
                editHMDaddressButton.setClickable(true);
                editHMDaddressButton.setEnabled(true);
                toggleARCoreSwitch.setClickable(true);
                toggleARCoreSwitch.setEnabled(true);
              }
            }

            // Repeat this runnable code block again every 10 ms
            handler.postDelayed(runnableCode, 10);
          }
        };

    // Start the initial runnable task by posting through the handler
    handler.post(runnableCode);
  }

  @Override
  protected void onDestroy() {
    ARCoreOnDestroy();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    ARCoreOnResume();
    sensorHandler.resumeAllSensorListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    ARCoreOnPause();
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

  private void initCommunicationHandler() {
    communicationHandler =  new CommunicationHandler(MainActivity.this);
  }

  private void disableSystemGestures() {
    // Disable system gestures on left and right edge to prevent accidental app closing
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      final View rootView = findViewById(android.R.id.content);
      rootView.post(() -> {
        int screenWidth = rootView.getWidth();
        int screenHeight = rootView.getHeight();

        // Define exclusion rects (e.g., 200 pixels from left and right edges)
        Rect leftEdge = new Rect(0, 0, 200, screenHeight);
        Rect rightEdge = new Rect(screenWidth - 200, 0, screenWidth, screenHeight);

        List<Rect> exclusionRects = new ArrayList<>();
        exclusionRects.add(leftEdge);
        exclusionRects.add(rightEdge);

        rootView.setSystemGestureExclusionRects(exclusionRects);
      });
    }
  }

  private void initUI() {
    // Load last HMD IP Address from memory
    SharedPreferences sharedPref = getSharedPreferences("DeviceInputXRPreferences", MODE_PRIVATE);
    hmdIPstring = sharedPref.getString("hmdIPstring", hmdIPstring);

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
              editHMDaddressButton.setClickable(false);
              editHMDaddressButton.setEnabled(false);
              toggleARCoreSwitch.setClickable(false);
              toggleARCoreSwitch.setEnabled(false);
            }
          }
        });

    toggleARCoreSwitch = findViewById(R.id.arcore_toggle);
    toggleARCoreSwitch.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              // Resume ARCore
              if (USE_AR_CORE) ARCoreOnResume();
            } else {
              // Pause ARCore
              if (USE_AR_CORE) ARCoreOnPause();
              pose = null;
              messageSnackbarHelper.hide(MainActivity.this);
            }
          }
        });

    editHMDaddressButton = findViewById(R.id.editHMDip_button);
    editHMDaddressButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            // Create an EditText
            final EditText input = new EditText(MainActivity.this);

            // Set up the AlertDialog
            new AlertDialog.Builder(MainActivity.this)
                // .setTitle("Edit IP Address of HMD to connect with")
                .setMessage("Enter the IP address of your HMD:")
                .setView(input)
                .setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int whichButton) {
                        // When the "OK" button is clicked, get the text from the EditText and
                        // assign it to your String variable

                        // only allow '0-9' and '.'
                        hmdIPstring = input.getText().toString().trim().replaceAll("[^0-9.]", "");

                        // save string to memory
                        SharedPreferences sharedPref =
                            getSharedPreferences("DeviceInputXRPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("hmdIPstring", hmdIPstring);
                        editor.apply();
                      }
                    })
                .setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int whichButton) {
                        // When the "Cancel" button is clicked, do nothing
                      }
                    })
                .show();
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
      connectionStatusText.setText("sending...");
      connectionIndicator.setColor(Color.parseColor("#ffb13d"));
    } else if (communicationHandler.isRunning() && communicationHandler.isConnected()) {
      connectionStatusText.setText("connected");
      connectionIndicator.setColor(Color.parseColor("#59c639"));
    } else {
      connectionStatusText.setText("not connected");
      connectionIndicator.setColor(Color.parseColor("#b5b5b5"));
    }
    deviceIPText.setText(getLocalIpAddress());
    hmdIPText.setText(hmdIPstring);

    if (sendingDataFlag) {
      tapsRemainingToStopConnection = tapsToStopConnection - touchHandler.getCurrentTapCount();
      connectButton.setText(
          "Tap anywhere " + tapsRemainingToStopConnection + " more times to disconnect");
    }

    // ------ ARCORE --------
    if (pose != null) {
      float[] position = pose.getTranslation();
      float[] rotation = pose.getRotationQuaternion();
      float[] eulerAngles = quaternionToEulerAngles(rotation);

      String msg =
          "("
              + String.format("%.2f", position[0])
              + ", "
              + String.format("%.2f", position[1])
              + ", "
              + String.format("%.2f", position[2])
              + ")";
      positionText.setText(msg);

      msg =
          "("
              + String.format("%.2f", 180f * (1 / Math.PI) * eulerAngles[0])
              + ", "
              + String.format("%.2f", 180f * (1 / Math.PI) * eulerAngles[1])
              + ", "
              + String.format("%.2f", 180f * (1 / Math.PI) * eulerAngles[2])
              + ")";
      orientationText.setText(msg);
    } else {
      String msg = "(0.00, 0.00, 0.00)";
      positionText.setText(msg);

      msg = "(0.00, 0.00, 0.00)";
      orientationText.setText(msg);
    }
  }

  /** Helper Functions */
  private static float[] quaternionToEulerAngles(float[] q) {

    float[] angles = new float[3];

    // roll (x-axis rotation)
    float sinr_cosp = 2 * (q[3] * q[0] + q[1] * q[2]);
    float cosr_cosp = 1 - 2 * (q[0] * q[0] + q[1] * q[1]);
    angles[0] = (float) Math.atan2(sinr_cosp, cosr_cosp);

    // pitch (y-axis rotation)
    float sinp = 2 * (q[3] * q[1] - q[2] * q[0]);
    if (Math.abs(sinp) >= 1) {
      angles[1] = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
    } else {
      angles[1] = (float) Math.asin(sinp);
    }

    // yaw (z-axis rotation)
    float siny_cosp = 2 * (q[3] * q[2] + q[0] * q[1]);
    float cosy_cosp = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
    angles[2] = (float) Math.atan2(siny_cosp, cosy_cosp);

    return angles;
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

  /**
   * ARCORE-RELATED VARIABLES & FUNCTIONS BELOW. (Should not need to modify.) Adapted from ARCore's
   * hello_ar_java /
   *
   * <p>/** ARCore Rendering Variables
   */
  private static final String SEARCHING_PLANE_MESSAGE = "Localizing using ARCore...";

  private GLSurfaceView surfaceView;
  private BackgroundRenderer backgroundRenderer;
  private boolean hasSetTextureNames = false;
  private boolean installRequested;

  /** ARCore Helpers */
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();

  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

  public void ARCoreOnCreate() {
    surfaceView = findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/* context= */ this);

    // Set up renderer.
    SampleRender render = new SampleRender(surfaceView, this, getAssets());
    installRequested = false;
  }

  public void ARCoreOnPause() {
    if (!USE_AR_CORE) return;

    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  public void ARCoreOnResume() {
    if (!USE_AR_CORE) return;

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Create the session.
        session = new Session(/* context= */ this);
      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      configureSession();
      // To record a live camera session for later playback, call
      // `session.startRecording(recordingConfig)` at anytime. To playback a previously recorded AR
      // session instead of using the live camera feed, call
      // `session.setPlaybackDatasetUri(Uri)` before calling `session.resume()`. To
      // learn more about recording and playback, see:
      // https://developers.google.com/ar/develop/java/recording-and-playback
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  public void ARCoreOnDestroy() {
    if (!USE_AR_CORE) return;

    if (session != null) {
      // Explicitly close ARCore Session to release native resources.
      // Review the API reference for important considerations before calling close() in apps with
      // more complicated lifecycle requirements:
      // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
      session.close();
      session = null;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      // Use toast instead of snackbar here since the activity will exit.
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
          .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(SampleRender render) {
    // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
    // an IOException.
    try {
      PlaneRenderer planeRenderer = new PlaneRenderer(render);
      backgroundRenderer = new BackgroundRenderer(render);
      /* width= */
      /* height= */ Framebuffer virtualSceneFramebuffer =
          new Framebuffer(render, /* width= */ 1, /* height= */ 1);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
    }
  }

  @Override
  public void onSurfaceChanged(SampleRender render, int width, int height) {
    // displayRotationHelper.onSurfaceChanged(width, height);
    // virtualSceneFramebuffer.resize(width, height);
  }

  /** Function called every frame, grabs latest ARCore pose */
  @Override
  public void onDrawFrame(SampleRender render) {
    if (!USE_AR_CORE || session == null) return;

    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(
          new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
      hasSetTextureNames = true;
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    // Obtain the current frame from the AR Session. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    try {
      frame = session.update();
      pose = frame.getAndroidSensorPose();

    } catch (CameraNotAvailableException e) {
      Log.e(TAG, "Camera not available during onDrawFrame", e);
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      return;
    }
    Camera camera = frame.getCamera();

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

    // Show a message based on whether tracking has failed, if planes are detected, and if the user
    // has placed any objects.
    String message = null;
    if (camera.getTrackingState() == TrackingState.PAUSED) {
      if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
        message = SEARCHING_PLANE_MESSAGE;
      } else {
        message = TrackingStateHelper.getTrackingFailureReasonString(camera);
      }
    } else {
      message = SEARCHING_PLANE_MESSAGE;
    }
    if (message == null) {
      messageSnackbarHelper.hide(this);
    } else {
      messageSnackbarHelper.showMessage(this, message);
    }
  }

  private void configureSession() {
    Config config = session.getConfig();
    config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
      config.setDepthMode(Config.DepthMode.AUTOMATIC);
    } else {
      config.setDepthMode(Config.DepthMode.DISABLED);
    }
    config.setInstantPlacementMode(InstantPlacementMode.DISABLED);
    session.configure(config);
  }
}
