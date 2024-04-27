package prefetcher

import chisel3._
import chisel3.util._

// TODO: update this module to implement Markov prefetcher's IO.
class MarkovPrefetcherIO extends Bundle {
  val addr = Input(UInt(32.W))
  val prefetch = Output(Bool())
  val prefetchAddr = Output(UInt(32.W))

  override def cloneType = new MarkovPrefetcherIO().asInstanceOf[this.type]
}