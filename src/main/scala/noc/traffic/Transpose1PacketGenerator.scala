package noc.traffic

import chisel3._
import noc.{NoCConfig, SyntheticPacketGenerator}

class Transpose1PacketGenerator(config: NoCConfig, num: Int, maxPackets: Int, injectionRate: Double, packetSize: Int, payloadIndexBits: Int, payloadDataBits: Int) extends SyntheticPacketGenerator(config, num, maxPackets, injectionRate, packetSize, payloadIndexBits, payloadDataBits) {
  private val (srcX, srcY) = (config.getX(io.packetOut.bits.src), config.getY(io.packetOut.bits.src))
  private val (destX, destY) = (config.width.U - 1.U - srcY, config.width.U - 1.U - srcX)

  io.packetOut.bits.dest := destY * config.width.U + destX
}