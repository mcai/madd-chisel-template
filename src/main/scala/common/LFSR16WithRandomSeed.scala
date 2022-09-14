package common

import chisel3._
import chisel3.util._

import scala.util.Random

object LFSR16WithRandomSeed {
  def apply(inc: Bool = true.B) = {
    val random = new Random()

    val seed = random.nextInt(2 << 15)

    val width = 16

    val lfsr = RegInit(seed.U(width.W))

    when(inc) {
      lfsr := Cat(lfsr(0) ^ lfsr(2) ^ lfsr(3) ^ lfsr(5), lfsr(width - 1, 1))
    }

    lfsr
  }
}

class LFSR16WithRandomSeedExampleIO extends Bundle {
  val lfsr1 = Output(UInt(16.W))
  val lfsr2 = Output(UInt(16.W))
}

class LFSR16WithRandomSeedExample extends Module {
  val io = IO(new LFSR16WithRandomSeedExampleIO)

  io.lfsr1 := LFSR16WithRandomSeed()
  io.lfsr2 := LFSR16WithRandomSeed()
}
