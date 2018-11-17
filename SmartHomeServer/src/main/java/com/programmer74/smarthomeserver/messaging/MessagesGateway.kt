package com.programmer74.smarthomeserver.messaging

import com.programmer74.smarthomeserver.communication.UDPGateway

import java.io.IOException
import java.util.HashSet

import com.programmer74.smarthomeserver.util.Utils.delay

class MessagesGateway(private val udpGateway: UDPGateway) {


  @Synchronized
  @Throws(IOException::class)
  operator fun get(nodeID: Int, regID: Int, payload: ByteArray): Message {

    val msg = Message(nodeID, Message.GET_RQ, regID)

    println("GET: Sending message " + msg)

    udpGateway.sendMessage(msg)

    val ans = udpGateway.retrieveMessageBlocking(nodeID)
    println("GET: Received message " + ans)

    return ans
  }

  @Synchronized
  @Throws(IOException::class)
  operator fun set(nodeID: Int, regID: Int, payload: ByteArray): Message {

    val msg = Message(nodeID, Message.SET_RQ, regID)
    msg.setPayload(payload, 0)
    udpGateway.sendMessage(msg)

    val ans = udpGateway.retrieveMessageBlocking(nodeID)
    println("SET: Received message " + ans)

    return ans
  }

  @Synchronized
  @Throws(IOException::class)
  fun ping(nodeID: Int): Message {

    pingWithoutReply(nodeID)

    val ans = udpGateway.retrieveMessageBlocking(nodeID)

    println("PING: Received message " + ans)

    return ans
  }

  @Synchronized
  @Throws(IOException::class)
  private fun pingWithoutReply(nodeID: Int) {

    val msg = Message(nodeID, Message.PING, 0)
    val payload = ByteArray(8)
    for (i in 0..7) {
      payload[i] = (System.currentTimeMillis() / (i + 1)).toByte()
    }
    msg.setPayload(payload, 0)

    println("PING: Sending message " + msg)

    udpGateway.sendMessage(msg)
  }

  //pingWithoutReply(i);
  val availableNodesList: Set<Int>
    @Synchronized @Throws(IOException::class)
    get() {
      val availableNodes = HashSet<Int>()

      val scanUpTo = 5

      for (i in 1 until scanUpTo) {
        pingWithoutReply(i)
        delay(50)
      }

      val timeout = System.currentTimeMillis() + 5000
      var ans: Message?
      while (System.currentTimeMillis() < timeout) {
        for (i in 1 until scanUpTo) {
          ans = udpGateway.retrieveMessage(i)
          if (ans != null) {
            udpGateway.clearMessage(i)
            availableNodes.add(i)
          } else if (!availableNodes.contains(i)) {
          }
          delay(10)
        }
      }

      return availableNodes
    }
}
