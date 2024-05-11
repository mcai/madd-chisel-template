package madd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MatrixAddition1Spec extends AnyFreeSpec with Matchers {
  "MatrixAddition1 should add two matrices" in {
    simulate(new MatrixAddition1(3, 2)) { dut =>
      for (i <- 0 until 3 * 2) {
        dut.io.a(i).poke(i.S)
        dut.io.b(i).poke(i.S)
      }
      dut.clock.step(1)

      for (i <- 0 until 3 * 2) {
        dut.io.out(i).expect((i * 2).S)
      }
    }
  }
}

object MatrixAddition1Tester extends App {
  (new MatrixAddition1Spec).execute()
}