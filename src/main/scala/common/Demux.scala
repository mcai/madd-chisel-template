package common

import chisel3._
import chisel3.util._

class Demux[T <: Data](gen: T, n: Int) extends Module {
  val io = IO(new Bundle {
    val select = Input(UInt(log2Ceil(n).W))
    val in = Flipped(Decoupled(gen))
    val out = Vec(n, Decoupled(gen))
  })

  io.in.ready := false.B

  io.out.zipWithIndex.foreach { case(out, i) =>
    out.bits := io.in.bits

    when(i.U =/= io.select) {
      out.valid := false.B
    }.otherwise {
      out.valid := io.in.valid

      io.in.ready := out.ready
    }
  }
}
