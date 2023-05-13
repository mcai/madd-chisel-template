package prefetcher

import chisel3._
import chisel3.util._

// TODO: update this module to implement stride prefetcher's IO.
class StridePrefetcherIO(M: Int, N: Int) extends Bundle {
  val a = Input(Vec(M * N, SInt(32.W)))
  val b = Input(Vec(M * N, SInt(32.W)))

  val out = Output(Vec(M * N, SInt(32.W)))

  override def cloneType =
    new StridePrefetcherIO(M, N).asInstanceOf[this.type]
}