package noc.routing

import noc.{Direction, NoCConfig, RoutingAlgorithm}
import chisel3._

class WestFirstRoutingAlgorithm(config: NoCConfig, val routerId: Int) extends RoutingAlgorithm(config) {
  io.dirs.foreach(_ := false.B)

  when(destX <= x || destY === y) {
    XYRouting(io.dirs, x, y, destX, destY)
  }.otherwise {
    when(destY < y) {
      io.dirs(Direction.north) := true.B
      io.dirs(Direction.east) := true.B
    }.otherwise {
      io.dirs(Direction.south) := true.B
      io.dirs(Direction.east) := true.B
    }
  }

  chisel3.printf(p"[$currentCycle WestFirstRouting] current = ${io.current}, src = ${io.src}, dest = ${io.dest}, dirs = ${io.dirs}\n")
}
