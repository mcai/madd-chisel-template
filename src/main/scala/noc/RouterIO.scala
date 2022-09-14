package noc

import chisel3._
import chisel3.util._

class RouterIO(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val packetIn = Vec(Direction.size, Flipped(Decoupled(new Packet(config, payloadIndexBits, payloadDataBits))))
  val packetOut = Vec(Direction.size, Decoupled(new Packet(config, payloadIndexBits, payloadDataBits)))

  val numFreeSlotsIn = Input(Vec(Direction.size, UInt(config.maxInputBufferSize.W)))
  val numFreeSlotsOut = Output(Vec(Direction.size, UInt(config.maxInputBufferSize.W)))

  override def cloneType = new RouterIO(config, payloadIndexBits, payloadDataBits).asInstanceOf[this.type]
}
