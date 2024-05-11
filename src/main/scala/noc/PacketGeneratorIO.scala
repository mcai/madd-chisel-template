package noc

import chisel3._
import chisel3.util._

class PacketGeneratorIO(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val packetOut = Decoupled(new Packet(config, payloadIndexBits, payloadDataBits))
}
