package madd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MatrixAddition1Spec extends AnyFreeSpec with Matchers {
  "MatrixAddition1 should add two matrices" in {
    simulate(new MatrixAddition1(3, 2)) { dut =>
      // Initializing matrix elements
      for (i <- 0 until 3 * 2) {
        dut.io.a(i).poke(i.S)
        dut.io.b(i).poke(i.S)
      }
      // Step the clock to process the addition
      dut.clock.step(1)

      // Checking the output of matrix addition
      for (i <- 0 until 3 * 2) {
        dut.io.out(i).expect((i * 2).S, s"Output at index $i should be ${i * 2} as it's the sum of $i and $i")
      }
    }
  }
}

object MatrixAddition1Tester extends App {
  (new MatrixAddition1Spec).execute()
}