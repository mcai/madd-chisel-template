package madd

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// TODO: update this module to implement stride prefetching.
class MatrixAddition1(M: Int, N: Int) extends Module {
  val io = IO(new MatrixAddition1IO(M, N))

  io.out := DontCare

  for (i <- 0 until M) {
    for (j <- 0 until N) {
      var sum = 0.S(32.W)

      sum = io.a(i * N + j) + io.b(i * N + j)

      io.out(i * N + j) := sum
    }
  }
}

object MatrixAddition1 extends App {
  ChiselStage.emitSystemVerilogFile(new MatrixAddition1(3, 2))
}