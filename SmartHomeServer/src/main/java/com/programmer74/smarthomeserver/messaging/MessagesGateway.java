package com.programmer74.smarthomeserver.messaging;

import com.programmer74.smarthomeserver.communication.UDPGateway;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.programmer74.smarthomeserver.util.Utils.delay;

public class MessagesGateway {

  private UDPGateway udpGateway;

  public MessagesGateway(UDPGateway udpGateway) {
    this.udpGateway = udpGateway;
  }


  public synchronized Message get(int nodeID, int regID, byte[] payload) throws IOException {

    Message msg = new Message(nodeID, Message.GET_RQ, regID);

    System.out.println("GET: Sending message " + msg);

    udpGateway.sendMessage(msg);

    Message ans =  udpGateway.retrieveMessageBlocking(nodeID);
    System.out.println("GET: Received message " + ans);

    return ans;
  }

  public synchronized Message set(int nodeID, int regID, byte[] payload) throws IOException {

    Message msg = new Message(nodeID, Message.SET_RQ, regID);
    msg.setPayload(payload, 0);
    udpGateway.sendMessage(msg);

    Message ans =  udpGateway.retrieveMessageBlocking(nodeID);
    System.out.println("SET: Received message " + ans);

    return ans;
  }

  public synchronized Message ping(int nodeID) throws IOException {

    Message msg = new Message(nodeID, Message.PING, 0);
    msg.setPayload(new byte[0], 0);

    udpGateway.sendMessage(msg);

    Message ans =  udpGateway.retrieveMessageBlocking(nodeID);

    System.out.println("PING: Received message " + ans);

    return ans;
  }

  private synchronized void pingWithoutReply(int nodeID) throws IOException {

    Message msg = new Message(nodeID, Message.PING, 0);
    msg.setPayload(new byte[0], 0);

    System.out.println("PING: Sending message " + msg);

    udpGateway.sendMessage(msg);
  }

  public synchronized Set<Integer> getAvailableNodesList() throws IOException {
    final Set<Integer> availableNodes = new HashSet<>();

    for (int i = 1; i < 10; i++) {
      pingWithoutReply(i);
      delay(50);
    }

    long timeout = System.currentTimeMillis() + 5000;
    Message ans;
    while (System.currentTimeMillis() < timeout) {
      for (int i = 0; i < 10; i++) {
        ans = udpGateway.retrieveMessage(i);
        if (ans != null) {
          udpGateway.clearMessage(i);
          availableNodes.add(i);
        }
        delay(10);
      }
    }

    return availableNodes;
  }
}
