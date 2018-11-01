package com.programmer74.smarthomeserver.communication;

public class Message {
  public static int GET_RQ = 0;
  public static int SET_RQ = 1;
  public static int GET_RP = 2;
  public static int SET_RP = 3;
  public static int PING = 4;
  public static int PONG = 5;

  public static int DIGITAL_WRITE = 0;
  public static int DIGITAL_READ = 1;
  public static int ANALOG_WRITE = 2;
  public static int ANALOG_READ = 3;

  private int nodeID;

  private int type;

  private int regID;

  private byte[] payload;

  private long timestamp;

  public Message(final int nodeID, final int type, final int regID) {
    this.nodeID = nodeID;
    this.type = type;
    this.regID = regID;
    this.payload = new byte[8];
    this.timestamp = System.currentTimeMillis();
  }

  public void setPayload(final byte payload) {
    this.payload[0] = payload;
  }

  public void setPayload(final byte[] data, int offset) {
    for (int i = 0; i + offset < data.length && i < payload.length; i++) {
      payload[i] = data[i + offset];
    }
  }

  public byte getBytePayload() {
    return payload[0];
  }

  public byte[] toBytes() {
    byte[] ans = new byte[11];
    ans[0] = (byte)nodeID;
    ans[1] = (byte)type;
    ans[2] = (byte)regID;
    for (int i = 0; i < payload.length; i++) {
      ans[3 + i] = payload[i];
    }
    return ans;
  }

  public static Message fromBytes(final byte[] data) {
    Message msg = new Message(data[0], data[1], data[2]);
    msg.setPayload(data, 3);
    return msg;
  }

  @Override public String toString() {
    String s = "Message{" +
        "nodeID=" + nodeID +
        ", type=" + type +
        ", regID=" + regID +
        ", payload=[";
    for (int i = 0; i < payload.length; i++) {
      s += String.format("%x,", payload[i]);
    }
    s += "]}";
    return s;
  }

  public int getNodeID() {
    return nodeID;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
