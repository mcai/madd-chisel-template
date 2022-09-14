package noc.routing

import noc.{Direction, NoCConfig, RoutingAlgorithm}
import chisel3._
import chisel3.util._

class OddEvenRoutingAlgorithm(config: NoCConfig, val routerId: Int) extends RoutingAlgorithm(config) {
  private def isEven(x: UInt) = x % 2.U === 0.U

  private def isOdd(x: UInt) = !isEven(x)

  io.dirs.foreach(_ := false.B)

  private val gt_e0 = destX >= x
  private val gt_e1 = y >= destY

  private val e0 = Mux(gt_e0, destX - x, x - destX)
  private val e1 = Mux(gt_e1, y - destY, destY - y)

  when(e0 === 0.U) {
    when(gt_e1 && e1 > 0.U) {
      io.dirs(Direction.north) := true.B
    }.otherwise {
      io.dirs(Direction.south) := true.B
    }
  }.otherwise {
    when(gt_e0 && e0 > 0.U) {
      when(e1 === 0.U) {
        io.dirs(Direction.east) := true.B
      }.otherwise {
        when(isOdd(x) || (x === srcX)) {
          when(gt_e1 && e1 > 0.U) {
            io.dirs(Direction.north) := true.B
          }.otherwise {
            io.dirs(Direction.south) := true.B
          }
        }
        when(isOdd(destX) || (!gt_e0 || (gt_e0 && e0 =/= 1.U))) {
          io.dirs(Direction.east) := true.B
        }
      }
    }.otherwise {
      io.dirs(Direction.west) := true.B
      when(isEven(x)) {
        when(gt_e1 && e1 > 0.U) {
          io.dirs(Direction.north) := true.B
        }
        when(!gt_e1 && e1 > 0.U) {
          io.dirs(Direction.south) := true.B
        }
      }
    }
  }

  chisel3.printf(p"[$currentCycle OddEvenRouting] current = ${io.current}, src = ${io.src}, dest = ${io.dest}, dirs = ${io.dirs}\n")
}
