package noc.routing

import noc.{Direction, NoCConfig, RoutingAlgorithm}
import chisel3._
import chisel3.util._

class NorthLastRoutingAlgorithm(config: NoCConfig, routerId: Int) extends RoutingAlgorithm(config) {
  io.dirs.foreach(_ := false.B)

  when(destX === x || destY <= y) {
    XYRouting(io.dirs, x, y, destX, destY)
  }.otherwise {
    when(destX < x) {
      io.dirs(Direction.south) := true.B
      io.dirs(Direction.west) := true.B
    }.otherwise {
      io.dirs(Direction.south) := true.B
      io.dirs(Direction.east) := true.B
    }
  }

  chisel3.printf(p"[$currentCycle NorthLastRouting] current = ${io.current}, src = ${io.src}, dest = ${io.dest}, dirs = ${io.dirs}\n")
}