package com.programmer74.smarthomeserver;

import com.programmer74.smarthomeserver.communication.UDPCommunication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetAddress;

@Configuration
public class SmarthomeserverConfig {

  @Bean
  public UDPCommunication udpCommunication() {
    try {
      return new UDPCommunication(InetAddress.getByName("localhost"), 1337);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
      return null;
    }
  }
}
