package noc

import chisel3._
import chisel3.util._
import common.CurrentCycle

abstract class AbstractPacketGenerator(val config: NoCConfig, val num: Int, val payloadIndexBits: Int, val payloadDataBits: Int) extends Module with CurrentCycle {
  val io = IO(new PacketGeneratorIO(config, payloadIndexBits, payloadDataBits))

  protected val (packetId, _) = Counter(io.packetOut.fire, Int.MaxValue)

  when(io.packetOut.fire) {
    chisel3.printf(p"[$currentCycle PacketGenerator#$num] Sent: ${io.packetOut.bits}\n")
  }
}
