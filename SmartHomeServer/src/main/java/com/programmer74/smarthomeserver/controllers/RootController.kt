package com.programmer74.smarthomeserver.controllers


import com.programmer74.smarthomeserver.AliveNodes
import com.programmer74.smarthomeserver.messaging.Message
import com.programmer74.smarthomeserver.communication.UDPGateway
import com.programmer74.smarthomeserver.messaging.MessagesGateway
import org.springframework.beans.factory.annotation.Autowired
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
  ): String {
    try {
      val msg = messagesGateway.get(nodeID!!, reg!!, ByteArray(0))
      return String.format("%f", msg.floatPayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return "Error: " + ex.message
    }

  }

  @GetMapping("set/{node}/{reg}/{val0}/{val1}")
  fun setRegister(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("reg") reg: Int?,
      @NotNull @PathVariable("val0") val0: Int?,
      @NotNull @PathVariable("val1") val1: Int?
  ): String {
    try {
      val payload = ByteArray(8)
      payload[0] = val0!!.toByte()
      payload[1] = val1!!.toByte()
      val msg = messagesGateway.set(nodeID!!, reg!!, payload)
      return String.format("%d", msg.bytePayload)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return "Error: " + ex.message
    }

  }

  @GetMapping("setRelay/{node}/{pin}/{val}")
  fun setRelay(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("pin") pin: Int?,
      @NotNull @PathVariable("val") `val`: Int?
  ): String {
    return setRegister(nodeID, Message.DIGITAL_WRITE, pin, `val`)
  }

  @GetMapping("setLed/{node}/{pin}/{val}")
  fun setLed(
      @NotNull @PathVariable("node") nodeID: Int?,
      @NotNull @PathVariable("pin") pin: Int?,
      @NotNull @PathVariable("val") `val`: Int?
  ): String {
    return setRegister(nodeID, Message.ANALOG_WRITE, pin, `val`)
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
