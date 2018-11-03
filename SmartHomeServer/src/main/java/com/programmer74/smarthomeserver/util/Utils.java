package com.programmer74.smarthomeserver.util;

public class Utils {

  public static void delay(int millis) {
    try {
      Thread.sleep(millis);
    } catch (Exception ex) {
      //nothing
    }
  }

}
