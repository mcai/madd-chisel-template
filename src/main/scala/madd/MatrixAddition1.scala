package madd

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class MatrixAddition1(M: Int, N: Int) extends Module {
  val io = IO(new MatrixAddition1IO(M, N))

  io.out := DontCare
  io.outValid := false.B

  when(io.inValid) {
    for (i <- 0 until M) {
      for (j <- 0 until N) {
        var sum = 0.S(32.W)

        sum = io.a(i * N + j) + io.b(i * N + j)

        io.out(i * N + j) := sum
      }
    }
    io.outValid := true.B
  }
}

object MatrixAddition1 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new MatrixAddition1(3, 2))
    )
  )
}