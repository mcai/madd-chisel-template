package prefetcher

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

// TODO: update this module to implement Markov prefetching.
class MarkovPrefetcher extends Module {
  val io = IO(new MarkovPrefetcherIO)

  // Implement your Markov prefetcher logic here

  io.prefetch := DontCare
  io.prefetchAddr := DontCare
}

object MarkovPrefetcherMain extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() => new MarkovPrefetcher))
  )
}