package cache

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.collection.mutable.ListBuffer

class Cache1Spec extends AnyFreeSpec with Matchers {
  "Cache1 should process requests and generate responses" in {
    simulate(new Cache1()) { dut =>
      // TODO: (writeEnable, address, data) -> (threadId, pc, writeEnable, address, writeData)
      var trace = new ListBuffer[(Boolean, Int, Int)]()

      val numAccesses = 1024

      for (i <- 0 until numAccesses) {
        trace += ((i % 2 == 0, i * 16 % 256, (i + 1))) // TODO: generate your data here as you like
      }

      // initialize the valid and ready bits to false
      dut.io.request.valid.poke(false.B)
      dut.io.response.ready.poke(false.B)

      // process the lines in the trace
      for (i <- 0 until numAccesses) {
        // wait until the request is ready
        while (dut.io.request.ready.peek() == false.B) {
          dut.clock.step(1)
        }

        // feed the request
        dut.io.request.valid.poke(true.B)
        dut.io.request.bits.address.poke(trace(i)._2.U)
        dut.io.request.bits.writeEnable.poke(trace(i)._1.B)

        // feed the write data if write is enabled
        if (trace(i)._1) {
          dut.io.request.bits.writeData.poke(trace(i)._3.U)
        }

        dut.clock.step(1)

        // mark the request as valid and response as ready
        dut.io.request.valid.poke(false.B)
        dut.io.response.ready.poke(true.B)

        // wait until the response is valid
        while (dut.io.response.valid.peek() == false.B) {
          dut.clock.step(1)
        }

        // TODO: you can print or expect (validate) the response here

        dut.clock.step(1)

        // mark the response as not ready again
        dut.io.response.ready.poke(false.B)
      }

      val numHits = dut.io.numHits.peek().litValue
      val numCycles = dut.io.numCycles.peek().litValue

      println(s"[Tester] numAccesses: ${numAccesses}, numHits: ${numHits}, hitRatio: ${numHits.toDouble / numAccesses}, numCycles: ${numCycles}")
    }
  }

}

object Cache1Tester extends App {
  (new Cache1Spec).execute()
}