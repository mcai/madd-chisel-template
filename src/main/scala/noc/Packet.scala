package noc

import chisel3._
import chisel3.util._

class Packet(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val id = UInt(32.W)

  val src = UInt(log2Ceil(config.numRouters).W)
  val dest = UInt(log2Ceil(config.numRouters).W)

  val payload = new Payload(config, payloadIndexBits, payloadDataBits)

  val size = UInt(log2Ceil(config.dataPacketSize + 1).W)

  val memories = Vec(config.diameter + 1, UInt(log2Ceil(config.numRouters).W))
  val numMemories = UInt(log2Ceil(config.diameter + 1).W)

  override def cloneType = new Packet(config, payloadIndexBits, payloadDataBits).asInstanceOf[this.type]

  def existsRouterIdInMemories(routerId: UInt) = {
    this.memories.zipWithIndex.map { case (memory, i) => i.U < this.numMemories && memory === routerId }.fold(false.B)(_ || _)
  }

  def indexOfRouterIdInMemories(routerId: UInt) = {
    PriorityMux(this.memories.zipWithIndex.map { case (memory, i) => (i.U < this.numMemories && memory === routerId, i.U) })
  }

  def lastIndexOfRouterIdInMemories(routerId: UInt) = {
    PriorityMux(this.memories.zipWithIndex.reverse.map { case (memory, i) => (i.U < this.numMemories && memory === routerId, i.U) })
  }
}
