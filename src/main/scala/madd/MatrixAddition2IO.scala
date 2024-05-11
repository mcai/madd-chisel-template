package madd

import chisel3._
import chisel3.util._

class MatrixAddition2IO(M: Int, N: Int) extends Bundle {
  val in = Flipped(DecoupledIO(new Bundle {
    val a = SInt(32.W)
    val b = SInt(32.W)
  }))

  val out = ValidIO(Vec(M * N, SInt(32.W)))
}