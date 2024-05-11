package madd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MatrixAddition2Spec extends AnyFreeSpec with Matchers {
  "MatrixAddition2 should add two matrices" in {
    simulate(new MatrixAddition2(3, 2)) { dut =>
      dut.io.in.valid.poke(true.B)

      for (i <- 0 until 3 * 2) {
        while (dut.io.in.ready.peek() == false.B) {
          dut.clock.step(1)
        }

        dut.io.in.bits.a.poke(i.S)
        dut.io.in.bits.b.poke(i.S)
        dut.clock.step(1)
      }

      dut.io.in.valid.poke(false.B)

      while (dut.io.out.valid.peek() == false.B) {
        dut.clock.step(1)
      }

      for (i <- 0 until 3 * 2) {
        dut.io.out.bits(i).expect((i * 2).S)
      }
    }
  }
}

object MatrixAddition2Tester extends App {
  (new MatrixAddition2Spec).execute()
}