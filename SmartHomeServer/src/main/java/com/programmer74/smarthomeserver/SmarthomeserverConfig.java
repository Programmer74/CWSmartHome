package com.programmer74.smarthomeserver;

import com.programmer74.smarthomeserver.communication.UDPGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetAddress;

@Configuration
public class SmarthomeserverConfig {

  @Bean
  public UDPGateway udpCommunication() {
    try {
      return new UDPGateway(InetAddress.getByName("localhost"), 1337);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
      return null;
    }
  }
}
