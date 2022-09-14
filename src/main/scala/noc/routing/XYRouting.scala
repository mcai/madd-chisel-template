package noc.routing

import noc.Direction
import common.CurrentCycle
import chisel3._
import chisel3.util._

object XYRouting {
  def apply[ParentT <: CurrentCycle](dirs: Vec[Bool], x: UInt, y: UInt, destX: UInt, destY: UInt) = {
    when(destX > x) {
      dirs(Direction.east.U) := true.B
    }.elsewhen(destX < x) {
      dirs(Direction.west.U) := true.B
    }.elsewhen(destY > y) {
      dirs(Direction.south.U) := true.B
    }.elsewhen(destY < y) {
      dirs(Direction.north.U) := true.B
    }
  }
}

