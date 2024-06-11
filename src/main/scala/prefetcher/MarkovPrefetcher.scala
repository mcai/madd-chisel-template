package prefetcher

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// TODO: update this module to implement Markov prefetching.
class MarkovPrefetcher extends Module {
  val io = IO(new MarkovPrefetcherIO)

  // Implement your Markov prefetcher logic here
  io.prefetch := DontCare
  io.prefetchAddress := DontCare
}

object MarkovPrefetcherMain extends App {
  ChiselStage.emitSystemVerilogFile(new MarkovPrefetcher)
}