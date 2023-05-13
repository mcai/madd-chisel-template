package prefetcher

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

// TODO: update this module to implement unit testing for stride prefetching.
class StridePrefetcherTester(dut: StridePrefetcher)
    extends PeekPokeTester(dut) {
  for (i <- 0 until 3 * 2) {
    poke(dut.io.a(i), i)
    poke(dut.io.b(i), i)
  }

  for (i <- 0 until 3 * 2) {
    expect(dut.io.out(i), i * 2)
  }
}

object StridePrefetcherTester extends App {
  chisel3.iotesters.Driver(() => new StridePrefetcher(3, 2)) { dut =>
    new StridePrefetcherTester(dut)
  }
}