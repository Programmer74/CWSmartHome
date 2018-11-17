package com.programmer74.smarthomeserver

data class AliveNodes (val status: String, val nodes: List<Int>)

data class FloatReply (val status: String, val value: Float)

data class ByteReply(val status: String, val value: Byte)

data class SetRegReply (val status: String, val reply: Int)