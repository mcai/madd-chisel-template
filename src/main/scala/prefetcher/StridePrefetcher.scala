package prefetcher

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

// TODO: update this module to implement stride prefetching.
class StridePrefetcher(M: Int, N: Int) extends Module {
  val io = IO(new StridePrefetcherIO(M, N))

  io.out := DontCare

  for (i <- 0 until M) {
    for (j <- 0 until N) {
      var sum = 0.S(32.W)

      sum = io.a(i * N + j) + io.b(i * N + j)

      io.out(i * N + j) := sum
    }
  }
}