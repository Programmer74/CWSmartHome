package com.programmer74.smarthomeserver.controllers


import com.programmer74.smarthomeserver.AliveNodes
import com.programmer74.smarthomeserver.FloatReply
import com.programmer74.smarthomeserver.ByteReply
import com.programmer74.smarthomeserver.SetRegReply
import com.programmer74.smarthomeserver.messaging.Message
import com.programmer74.smarthomeserver.messaging.MessagesGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull
import java.io.IOException

@RestController
@RequestMapping(path = arrayOf("/api"))
class RootController (private val messagesGateway: MessagesGateway){

  @GetMapping("get/{node}/{reg}")
  fun getRegister(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("reg") reg: Int?
  ): String {
    try {
      val msg = messagesGateway.get(nodeID!!, reg!!, ByteArray(0))
      return String.format("%d", msg.bytePayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return "Error: " + ex.message
    }

  }

  @GetMapping("get/{node}/{reg}/float")
  fun getRegisterFloat(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("reg") reg: Int?
  ): FloatReply {
    try {
      val msg = messagesGateway.get(nodeID!!, reg!!, ByteArray(0))
      return FloatReply("ok", msg.floatPayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return FloatReply("Error: " + ex.message, 0f)
    }

  }

  @GetMapping("get/{node}/{reg}/byte")
  fun getRegisterByte(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("reg") reg: Int?
  ): ByteReply {
    try {
      val msg = messagesGateway.get(nodeID!!, reg!!, ByteArray(0))
      return ByteReply("ok", msg.bytePayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return ByteReply("Error: " + ex.message, 0)
    }

  }

  @GetMapping("set/{node}/{reg}/{val0}/{val1}")
  fun setRegisterByte(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("reg") reg: Int?,
      @NotNull @PathVariable("val0") val0: Int?,
      @NotNull @PathVariable("val1") val1: Int?
  ): SetRegReply {
    try {
      val payload = ByteArray(8)
      payload[0] = val0!!.toByte()
      payload[1] = val1!!.toByte()
      val msg = messagesGateway.set(nodeID!!, reg!!, payload)
      return SetRegReply("ok", msg.bytePayload.toInt())
    } catch (ex: IOException) {
      ex.printStackTrace()
      return SetRegReply("Error: " + ex.message, -1)
    }

  }

  @GetMapping("setRelay/{node}/{pin}/{val}")
  fun setRelay(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("pin") pin: Int?,
      @NotNull @PathVariable("val") value: Int?
  ): SetRegReply {
    if (value != 0) {
      return setRegisterByte(nodeID, Message.RELAY_ON, pin, value)
    } else {
      return setRegisterByte(nodeID, Message.RELAY_OFF, pin, value)
    }
  }

  @GetMapping("digitalWrite/{node}/{pin}/{val}")
  fun digitalWrite(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("pin") pin: Int?,
      @NotNull @PathVariable("val") value: Int?
  ): SetRegReply {
    return setRegisterByte(nodeID, Message.DIGITAL_WRITE, pin, value)
  }

  @GetMapping("analogWrite/{node}/{pin}/{val}")
  fun analogWrite(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("pin") pin: Int?,
      @NotNull @PathVariable("val") value: Int?
  ): SetRegReply {
    return setRegisterByte(nodeID, Message.ANALOG_WRITE, pin, value)
  }

  @GetMapping("ping/{node}")
  fun performPing(
      @NotNull @PathVariable("node") nodeID: Int?
  ): String {
    try {
      val msg = messagesGateway.ping(nodeID!!)
      return String.format("%d", msg.bytePayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return "Error: " + ex.message
    }

  }

  @GetMapping("alive")
  fun performAliveNodesLookup(): AliveNodes {
    try {
      val nodes = messagesGateway.availableNodesList
      return AliveNodes("ok", nodes.toList())
    } catch (ex: IOException) {
      ex.printStackTrace()
      return AliveNodes("error", emptyList())
    }
  }
}
