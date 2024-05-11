package madd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MatrixAddition2Spec extends AnyFreeSpec with Matchers {
  "MatrixAddition2 should add two matrices" in {
    simulate(new MatrixAddition2(3, 2)) { dut =>
      // Assert input valid signal
      dut.io.in.valid.poke(true.B)

      // Input matrix elements as long as the input is ready to accept them
      for (i <- 0 until 3 * 2) {
        while (dut.io.in.ready.peek() == false.B) {
          dut.clock.step(1)  // Wait for input ready
        }

        dut.io.in.bits.a.poke(i.S)  // Poke value to matrix 'a'
        dut.io.in.bits.b.poke(i.S)  // Poke same value to matrix 'b'
        dut.clock.step(1)  // Process the input for next cycle
      }

      // Signal the end of input
      dut.io.in.valid.poke(false.B)

      // Wait for output to become valid
      while (dut.io.out.valid.peek() == false.B) {
        dut.clock.step(1)  // Step the clock until output is valid
      }

      // Verify the outputs of matrix addition
      for (i <- 0 until 3 * 2) {
        dut.io.out.bits(i).expect((i * 2).S, s"Expected output for element $i to be ${i * 2} but found ${dut.io.out.bits(i).peek().litValue}")
      }
    }
  }
}

object MatrixAddition2Tester extends App {
  (new MatrixAddition2Spec).execute()
}