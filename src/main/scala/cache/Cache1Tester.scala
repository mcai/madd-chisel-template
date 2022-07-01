package cache

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

class Cache1Tester(dut: Cache1) extends PeekPokeTester(dut) {
    // TODO
}

object Cache1Tester extends App {
  chisel3.iotesters.Driver(() => new Cache1()) { dut =>
    new Cache1Tester(dut)
  }
}