package noc

import chisel3._
import chisel3.util._

class NoCSimulatorIO(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val packetIn = Flipped(Decoupled(new Packet(config, payloadIndexBits, payloadDataBits)))
  val packetOut = Decoupled(new Packet(config, payloadIndexBits, payloadDataBits))

  val numDataPacketsReceived = Output(UInt(32.W))
  val numDataPacketsSent = Output(UInt(32.W))
}
