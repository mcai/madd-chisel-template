package noc.routing

import noc.{NoCConfig, RoutingAlgorithm}
import chisel3._
import chisel3.util._

class XYRoutingAlgorithm(config: NoCConfig, routerId: Int) extends RoutingAlgorithm(config) {
  io.dirs.foreach(_ := false.B)

  XYRouting(io.dirs, x, y, destX, destY)

  chisel3.printf(p"[$currentCycle XYRouting] current = ${io.current}, src = ${io.src}, dest = ${io.dest}, dirs = ${io.dirs}\n")
}


