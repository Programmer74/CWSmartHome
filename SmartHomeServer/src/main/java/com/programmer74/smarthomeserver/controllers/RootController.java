package com.programmer74.smarthomeserver.controllers;

import com.programmer74.smarthomeserver.communication.Message;
import com.programmer74.smarthomeserver.communication.UDPGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Set;

@RestController
public class RootController {

  @Autowired
  private UDPGateway udp;

  @GetMapping("get/{node}/{reg}")
  public String getRegister(
      @NotNull @PathVariable("node") Integer nodeID,
      @NotNull @PathVariable("reg") Integer reg
  ) {
    try {
      Message msg = udp.get(nodeID, reg, new byte[0]);
      return String.format("%d", msg.getBytePayload());
    } catch (IOException ex) {
      ex.printStackTrace();
      return "Error: " + ex.getMessage();
    }
  }

  @GetMapping("set/{node}/{reg}/{val0}/{val1}")
  public String setRegister(
      @NotNull @PathVariable("node") Integer nodeID,
      @NotNull @PathVariable("reg") Integer reg,
      @NotNull @PathVariable("val0") Integer val0,
      @NotNull @PathVariable("val1") Integer val1
  ) {
    try {
      byte[] payload = new byte[8];
      payload[0] = val0.byteValue();
      payload[1] = val1.byteValue();
      Message msg = udp.set(nodeID, reg, payload);
      return String.format("%d", msg.getBytePayload());
    } catch (IOException ex) {
      ex.printStackTrace();
      return "Error: " + ex.getMessage();
    }
  }

  @GetMapping("setLed/{node}/{pin}/{val}")
  public String setLed(
      @NotNull @PathVariable("node") Integer nodeID,
      @NotNull @PathVariable("pin") Integer pin,
      @NotNull @PathVariable("val") Integer val
  ) {
    return setRegister(nodeID, Message.DIGITAL_WRITE, pin, val);
  }

  @GetMapping("ping/{node}")
  public String performPing(
      @NotNull @PathVariable("node") Integer nodeID
  ) {
    try {
      Message msg = udp.ping(nodeID);
      return String.format("%d", msg.getBytePayload());
    } catch (IOException ex) {
      ex.printStackTrace();
      return "Error: " + ex.getMessage();
    }
  }

  @GetMapping("alive")
  public String performAliveNodesLookup() {
    try {
      Set<Integer> nodes = udp.getAvailableNodesList();
      return nodes.toString();
    } catch (IOException ex) {
      ex.printStackTrace();
      return "Error: " + ex.getMessage();
    }
  }
}
