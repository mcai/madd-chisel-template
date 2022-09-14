package noc

import chisel3._
import chisel3.util._

class NoCSimulatorTopIO(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val currentCycle = Output(UInt(32.W))

  val debugDataPacketIn = Valid(new Packet(config, payloadIndexBits, payloadDataBits))
  val debugDataPacketOut = Valid(new Packet(config, payloadIndexBits, payloadDataBits))

  val numDataPacketsReceived = Output(UInt(32.W))
  val numDataPacketsSent = Output(UInt(32.W))

  override def cloneType = new NoCSimulatorTopIO(config, payloadIndexBits, payloadDataBits).asInstanceOf[this.type]
}

