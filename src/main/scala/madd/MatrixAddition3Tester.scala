package madd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MatrixAddition3Spec extends AnyFreeSpec with Matchers {
  "MatrixAddition3 should add two matrices" in {
    simulate(new MatrixAddition3(3, 2)) { dut =>
      // Activate the valid signal for input
      dut.io.in.valid.poke(true.B)

      // Feed input data to the module as long as it's ready to accept it
      for (i <- 0 until 3 * 2) {
        while (dut.io.in.ready.peek() == false.B) {
          dut.clock.step(1)  // Step the clock until ready signal is asserted
        }

        dut.io.in.bits.a.poke(i.S)  // Provide input for matrix 'a'
        dut.io.in.bits.b.poke(i.S)  // Provide the same input for matrix 'b'
        dut.clock.step(1)  // Process the input
      }

      // Deactivate the valid signal indicating no further inputs
      dut.io.in.valid.poke(false.B)

      // Wait until the output data is ready for retrieval
      while (dut.io.out.valid.peek() == false.B) {
        dut.clock.step(1)  // Continue stepping the clock until output is valid
      }

      // Validate the output of the matrix addition
      for (i <- 0 until 3 * 2) {
        dut.io.out.bits(i).expect((i * 2).S, s"Output element $i should be ${i * 2} but was ${dut.io.out.bits(i).peek().litValue}")
      }
    }
  }
}

object MatrixAddition3Tester extends App {
  (new MatrixAddition3Spec).execute()
}