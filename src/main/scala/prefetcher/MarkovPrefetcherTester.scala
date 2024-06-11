package prefetcher

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MarkovPrefetcherSpec extends AnyFreeSpec with Matchers {
  "MarkovPrefetcher should predict the next address based on Markov chain" in {
    simulate(new MarkovPrefetcher()) { dut =>
      // Test case 1: Simple sequence
      val addresses1 = Seq(0x100, 0x104, 0x108, 0x10C, 0x110)
      for (address <- addresses1) {
        dut.io.address.poke(address.U)
        dut.clock.step(1)
        dut.io.prefetch.expect(true.B, s"Prefetch signal should be true for address ${address.toHexString}")
        dut.io.prefetchAddress.expect((address + 4).U, s"Prefetch address should be ${address.toHexString} + 4")
      }

      // Test case 2: Repetitive sequence
      val addresses2 = Seq(0x200, 0x204, 0x200, 0x204, 0x200, 0x204)
      for (address <- addresses2) {
        dut.io.address.poke(address.U)
        dut.clock.step(1)
        dut.io.prefetch.expect(true.B, s"Prefetch signal should be true for repetitive sequence at address ${address.toHexString}")
        dut.io.prefetchAddress.expect(if (address == 0x200) 0x204.U else 0x200.U, s"Prefetch address should toggle between 0x200 and 0x204, current address: ${address.toHexString}")
      }

      // Add more test cases as needed
    }
  }
}

object MarkovPrefetcherTester extends App {
  (new MarkovPrefetcherSpec).execute()
}