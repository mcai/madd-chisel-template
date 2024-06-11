package prefetcher

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MarkovPrefetcherSpec extends AnyFreeSpec with Matchers {
  "MarkovPrefetcher 应该根据马尔可夫链预测下一个地址" in {
    simulate(new MarkovPrefetcher()) { dut =>
      // 测试用例 1: 简单序列
      val addresses1 = Seq(0x100, 0x104, 0x108, 0x10C, 0x110)
      for (address <- addresses1) {
        dut.io.address.poke(address.U)
        dut.clock.step(1)
        dut.io.prefetch.expect(true.B, s"对于地址 ${address.toHexString}，预取信号应为 true")
        dut.io.prefetchAddress.expect((address + 4).U, s"预取地址应为 ${address.toHexString} + 4")
      }

      // 测试用例 2: 重复序列
      val addresses2 = Seq(0x200, 0x204, 0x200, 0x204, 0x200, 0x204)
      for (address <- addresses2) {
        dut.io.address.poke(address.U)
        dut.clock.step(1)
        dut.io.prefetch.expect(true.B, s"对于重复序列在地址 ${address.toHexString}，预取信号应为 true")
        dut.io.prefetchAddress.expect(if (address == 0x200) 0x204.U else 0x200.U, s"预取地址应在 0x200 和 0x204 之间切换，当前地址: ${address.toHexString}")
      }

      // 根据需要添加更多测试用例
    }
  }
}

object MarkovPrefetcherTester extends App {
  (new MarkovPrefetcherSpec).execute()
}