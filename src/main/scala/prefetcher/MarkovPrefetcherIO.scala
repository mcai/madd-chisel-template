package prefetcher

import chisel3._
import chisel3.util._

// TODO: 更新此模块以实现马尔可夫预取器的 IO。
class MarkovPrefetcherIO extends Bundle {
  val address = Input(UInt(32.W))
  val prefetch = Output(Bool())
  val prefetchAddress = Output(UInt(32.W))
}