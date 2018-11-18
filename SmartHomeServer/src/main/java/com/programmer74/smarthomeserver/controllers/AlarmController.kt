package com.programmer74.smarthomeserver.controllers

import com.programmer74.smarthomeserver.ByteReply
import com.programmer74.smarthomeserver.LongReply
import com.programmer74.smarthomeserver.messaging.MessagesGateway
import com.programmer74.smarthomeserver.watchers.AlarmWatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct

@RestController
@RequestMapping(path = arrayOf("/api/alarm"))
class AlarmController (private val messagesGateway: MessagesGateway,
                       private val alarmWatcher: AlarmWatcher) {

  @PostConstruct
  fun wtf() {
    alarmWatcher.enableAlarmWatcher()
  }

  @GetMapping("lastAlarm")
  fun getLastAlarm(): LongReply {
    return LongReply("ok", alarmWatcher.lastAlarmTimestamp)
  }

  @GetMapping("totalAlarms")
  fun totalAlarms(): LongReply {
    return LongReply("ok", alarmWatcher.totalAlarms)
  }

  @GetMapping("current")
  fun currentStatus(): ByteReply {
    try {
      val msg = messagesGateway.get(alarmWatcher.nodeID, 1, ByteArray(0))

      if (msg.bytePayload.toInt() == 1) {
        if (alarmWatcher.lastAlarmTimestamp != msg.timestamp) {
          alarmWatcher.totalAlarms++
          println("ALARMCONTROLLER: Got alarm at ${Date()}")
        }
        alarmWatcher.lastAlarmTimestamp = msg.timestamp
      }

      return ByteReply("ok", msg.bytePayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return ByteReply("Error: " + ex.message, 0)
    }
  }
}