package com.programmer74.smarthomeserver.communication;

import com.programmer74.smarthomeserver.messaging.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.programmer74.smarthomeserver.util.Utils.delay;

public class UDPGateway {
  private DatagramSocket socket;
  private InetAddress serverAddress;
  private int serverPort;

  private byte[] receiveData = new byte[10];
  private DatagramPacket receivePacket;

  private Thread listenerThread;

  private Map<Integer, Message> responseMap = new ConcurrentHashMap<>();

  public UDPGateway(final InetAddress serverAddress, final int serverPort) {
    try {
      this.socket = new DatagramSocket(1338);
      this.serverAddress = serverAddress;
      this.serverPort = serverPort;
      this.socket.setSoTimeout(5000);

      this.receivePacket = new DatagramPacket(receiveData,
          receiveData.length);

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
    }

    setupListenerThread();
    listenerThread.start();
  }

  private void setupListenerThread() {
    listenerThread = new Thread(() -> {
      while (true) {
        try {
          socket.receive(receivePacket);
          Message ans = Message.fromBytes(receivePacket.getData());
          System.out.println("THREAD: Received message " + ans);
          responseMap.put(ans.getNodeID(), ans);
        } catch (SocketTimeoutException stex) {
          //do nothing on socket timeout
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  public synchronized void clearMessage(int nodeID) {
    responseMap.remove(nodeID);
  }

  public synchronized Message retrieveMessageBlocking(int nodeID) throws IOException {
    Message ans = null;
    long timeout = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < timeout) {
      ans = responseMap.get(nodeID);
      if (ans != null) {
        break;
      }
      delay(10);
    }

    if (ans == null) {
      throw new IOException("Timeout");
    }
    return ans;
  }

  public synchronized Message retrieveMessage(int nodeID) {
    return responseMap.get(nodeID);
  }

  public synchronized void sendMessage(Message msg) throws IOException {

    final byte[] sendData = msg.toBytes();

    final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
        serverAddress, serverPort);

    clearMessage(msg.getNodeID());
    socket.send(sendPacket);
  }
}
