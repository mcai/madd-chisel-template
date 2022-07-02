package common

import chisel3._
import chisel3.util._

object CurrentCycle {
  def apply(): UInt = {
    val regCurrentCycle = RegInit(0.U(32.W))
    regCurrentCycle := regCurrentCycle + 1.U
    regCurrentCycle
  }
}

trait CurrentCycle {
  val currentCycle = CurrentCycle()
}