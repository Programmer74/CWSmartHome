package com.programmer74.smarthomeserver.watchers

import com.programmer74.smarthomeserver.communication.UDPGateway
import com.programmer74.smarthomeserver.messaging.Message
import java.util.*

class AlarmWatcher(udpGateway: UDPGateway, val nodeID: Int) {

  private val alarmWatcherThread: Thread
  var lastAlarmTimestamp = 0L
  var totalAlarms = 0L

  fun enableAlarmWatcher() {
    alarmWatcherThread.start()
  }

  init {
    alarmWatcherThread = Thread {
      while (true) {
        val msg = udpGateway.retrieveMessage(nodeID)
        if ((msg != null) && (msg.type == Message.ALARM)) {
          if (lastAlarmTimestamp != msg.timestamp) {
            totalAlarms++
            println("ALARMWATCHER: Got alarm at ${Date()}")
          }
          lastAlarmTimestamp = msg.timestamp
        }
        Thread.sleep(500)
      }
    }
  }
}