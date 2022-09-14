package noc.traffic

import chisel3._
import noc.{NoCConfig, SyntheticPacketGenerator}
import common.LFSR16WithRandomSeed

class UniformPacketGenerator(config: NoCConfig, num: Int, maxPackets: Int, injectionRate: Double, packetSize: Int, payloadIndexBits: Int, payloadDataBits: Int) extends SyntheticPacketGenerator(config, num, maxPackets, injectionRate, packetSize, payloadIndexBits, payloadDataBits) {
  io.packetOut.bits.dest := LFSR16WithRandomSeed() % config.numRouters.U
}
