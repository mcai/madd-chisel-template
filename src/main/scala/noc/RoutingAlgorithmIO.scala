package noc

import chisel3._
import chisel3.util._

class RoutingAlgorithmIO(val config: NoCConfig) extends Bundle {
  val current = Input(UInt(log2Ceil(config.numRouters).W))
  val src = Input(UInt(log2Ceil(config.numRouters).W))
  val dest = Input(UInt(log2Ceil(config.numRouters).W))

  val dirs = Output(Vec(Direction.size, Bool()))
}
