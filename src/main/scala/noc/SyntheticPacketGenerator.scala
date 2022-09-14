package noc

import chisel3._
import chisel3.util._
import common.LFSR16WithRandomSeed

abstract class SyntheticPacketGenerator(config: NoCConfig, num: Int, val maxPackets: Int, val injectionRate: Double, val packetSize: Int, payloadIndexBits: Int, payloadDataBits: Int)
  extends AbstractPacketGenerator(config, num, payloadIndexBits, payloadDataBits) {
  private val srcNotEqualsDest = io.packetOut.bits.src =/= io.packetOut.bits.dest

  private val limitedByInjectionRate: Bool = LFSR16WithRandomSeed() % 100.U <= (injectionRate * 100 * config.numRouters).toInt.U

  private val notExceedsMaxPackets: Bool = (maxPackets == -1).B || packetId < maxPackets.U

  io.packetOut.valid := srcNotEqualsDest && limitedByInjectionRate && notExceedsMaxPackets

  io.packetOut.bits.id := packetId

  io.packetOut.bits.src := LFSR16WithRandomSeed() % config.numRouters.U

  io.packetOut.bits.payload.index := 0.U
  io.packetOut.bits.payload.data := 0.S

  io.packetOut.bits.size := packetSize.U

  io.packetOut.bits.memories.foreach(_ := 0.U)
  io.packetOut.bits.numMemories := 0.U
}
