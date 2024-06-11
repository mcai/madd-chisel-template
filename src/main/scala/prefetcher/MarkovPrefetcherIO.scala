package prefetcher

import chisel3._
import chisel3.util._

// TODO: update this module to implement Markov prefetcher's IO.
class MarkovPrefetcherIO extends Bundle {
  val address = Input(UInt(32.W))
  val prefetch = Output(Bool())
  val prefetchAddress = Output(UInt(32.W))
}