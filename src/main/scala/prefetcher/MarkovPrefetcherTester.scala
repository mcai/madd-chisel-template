package prefetcher

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

// TODO: update this module to implement unit testing for Markov prefetching.
class MarkovPrefetcherTester(dut: MarkovPrefetcher) extends PeekPokeTester(dut) {
  // Test case 1: Simple sequence
  val addresses1 = Seq(0x100, 0x104, 0x108, 0x10C, 0x110)
  for (addr <- addresses1) {
    poke(dut.io.addr, addr)
    step(1)
    expect(dut.io.prefetch, 1)
    expect(dut.io.prefetchAddr, addr + 4)
  }

  // Test case 2: Repetitive sequence
  val addresses2 = Seq(0x200, 0x204, 0x200, 0x204, 0x200, 0x204)
  for (addr <- addresses2) {
    poke(dut.io.addr, addr)
    step(1)
    expect(dut.io.prefetch, 1)
    expect(dut.io.prefetchAddr, if (addr == 0x200) 0x204 else 0x200)
  }

  // Add more test cases as needed

}

object MarkovPrefetcherTester extends App {
  chisel3.iotesters.Driver(() => new MarkovPrefetcher()) { dut =>
    new MarkovPrefetcherTester(dut)
  }
}