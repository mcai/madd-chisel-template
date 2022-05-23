package madd

import chisel3._
import chisel3.util._

object CurrentCycle {
  def apply(): UInt = {
    val (currentCycle, _) = Counter(true.B, Int.MaxValue)
    RegNext(currentCycle, 0.U)
  }
}

trait CurrentCycle {
  val currentCycle = CurrentCycle()
}