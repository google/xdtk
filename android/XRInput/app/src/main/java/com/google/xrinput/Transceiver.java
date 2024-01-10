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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Transceiver {
  private final String TAG = Transceiver.class.getSimpleName();

  private CommunicationHandler communicationHandler;
  private DatagramSocket socket;
  private InetAddress address;
  private BlockingQueue<String> messageQueue;
  private volatile boolean running;
  private byte[] recvBuffer = new byte[1024]; // Adjust the size as needed
  private DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

  public Transceiver(
      String ipAddress, int sendPort, int receivePort, CommunicationHandler communicationHandler) {
    this.communicationHandler = communicationHandler;

    try {
      Log.d(TAG, "Setting up UDP sender...");
      socket = new DatagramSocket(receivePort);
      address = InetAddress.getByName(ipAddress);
      this.messageQueue = new LinkedBlockingQueue<>();
    } catch (SocketException | UnknownHostException e) {
      Log.d(TAG, "Error occurred when setting up UDP socket");
      e.printStackTrace();
    }

    this.running = true;

    startListening();

    new Thread(
            () -> {
              while (running) {
                try {
                  // Log.d(TAG,"Running! Waiting for packet to send...");
                  String message = messageQueue.take(); // This will block if the queue is empty
                  byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                  DatagramPacket packet =
                      new DatagramPacket(buffer, buffer.length, address, sendPort);
                  // Log.d(TAG,"Sending message: " + message);
                  socket.send(packet);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();
  }

  public void startListening() {
    new Thread(
            () -> {
              while (running) {
                try {
                  // This will block until a packet is received
                  Log.d(TAG, "Listening! Waiting to receive a packet...");
                  socket.receive(recvPacket);

                  String receivedMessage =
                      new String(
                          recvPacket.getData(), 0, recvPacket.getLength(), StandardCharsets.UTF_8);
                  Log.d(TAG, "Received: " + receivedMessage);

                  // give message back up to communication handler to parse...
                  communicationHandler.parseReceivedMessage(receivedMessage);

                  // Reset the packet length for the next receive
                  recvPacket.setLength(recvBuffer.length);
                } catch (IOException e) {
                  e.printStackTrace();
                  // If an exception occurs, stop running
                  running = false;
                }
              }
            })
        .start();
  }

  public void sendData(String data) {
    // pre-append timestamp
    long timestamp = System.currentTimeMillis();
    String dataToSend = timestamp + "," + data;

    // add to message queue
    messageQueue.add(dataToSend);
  }

  public void close() {
    Log.d(TAG, "Closing UDP...");
    this.running = false;
    socket.close();
  }

  public boolean isRunning() {
    return running;
  }
}
