package com.programmer74.smarthomeserver;

import com.programmer74.smarthomeserver.communication.UDPGateway;
import com.programmer74.smarthomeserver.messaging.MessagesGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetAddress;

@Configuration
public class SmarthomeserverConfig {

  @Autowired
  private ApplicationArguments applicationArguments;

  @Bean
  public UDPGateway udpCommunication() {
    try {
      return new UDPGateway(InetAddress.getByName(applicationArguments.getSourceArgs()[0]), 1337);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

  @Bean
  public MessagesGateway messageGateway(UDPGateway udpGateway) {
    return new MessagesGateway(udpGateway);
  }
}
