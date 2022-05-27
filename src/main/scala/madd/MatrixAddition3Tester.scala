package madd

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

class MatrixAddition3Tester(dut: MatrixAddition3)
    extends PeekPokeTester(dut) {
  poke(dut.io.in.valid, true)

  for (i <- 0 until 3 * 2) {
    while (peek(dut.io.in.ready) == BigInt(0)) {
      step(1)
    }

    poke(dut.io.in.bits.a, i)
    poke(dut.io.in.bits.b, i)

    step(1)
  }

  poke(dut.io.in.valid, false)

  while (peek(dut.io.out.valid) == BigInt(0)) {
    step(1)
  }

  for (i <- 0 until 3 * 2) {
    expect(dut.io.out.bits(i), i * 2)
  }
}

object MatrixAddition3Tester extends App {
  chisel3.iotesters.Driver(() => new MatrixAddition3(3, 2)) { dut =>
    new MatrixAddition3Tester(dut)
  }
}