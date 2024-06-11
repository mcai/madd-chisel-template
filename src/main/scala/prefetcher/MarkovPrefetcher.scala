package prefetcher

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// TODO: 更新此模块以实现马尔可夫预取。
class MarkovPrefetcher extends Module {
  val io = IO(new MarkovPrefetcherIO)

  // 在此处实现你的马尔可夫预取逻辑
  io.prefetch := DontCare
  io.prefetchAddress := DontCare
}

object MarkovPrefetcherMain extends App {
  ChiselStage.emitSystemVerilogFile(new MarkovPrefetcher)
}