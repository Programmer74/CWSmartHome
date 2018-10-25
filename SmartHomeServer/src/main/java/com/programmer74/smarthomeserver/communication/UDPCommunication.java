package com.programmer74.smarthomeserver.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPCommunication {
  private DatagramSocket socket;
  private InetAddress serverAddress;
  private int serverPort;

  private byte[] receiveData = new byte[10];
  private DatagramPacket receivePacket;

  public UDPCommunication(final InetAddress serverAddress, final int serverPort) {
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
  }

  public synchronized Message get(int nodeID, int regID, byte[] payload) throws IOException {

    Message msg = new Message(nodeID, Message.GET_RQ, regID);

    final byte[] sendData = msg.toBytes();

    final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
        serverAddress, serverPort);

    System.out.println("GET: Sending message " + msg);
    socket.send(sendPacket);
    socket.receive(receivePacket);

    Message ans = Message.fromBytes(receivePacket.getData());
    System.out.println("GET: Received message " + ans);

    return ans;
  }

  public synchronized Message set(int nodeID, int regID, byte[] payload) throws IOException {

    Message msg = new Message(nodeID, Message.SET_RQ, regID);
    msg.setPayload(payload, 0);
    final byte[] sendData = msg.toBytes();

    final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
        serverAddress, serverPort);

    System.out.println("SET: Sending message " + msg);
    socket.send(sendPacket);
    socket.receive(receivePacket);

    Message ans = Message.fromBytes(receivePacket.getData());
    System.out.println("SET: Received message " + ans);

    return ans;
  }
}
