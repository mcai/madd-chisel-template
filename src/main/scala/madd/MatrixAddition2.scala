package madd

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import common.CurrentCycle

class MatrixAddition2(M: Int, N: Int)
    extends Module
    with CurrentCycle {
  val io = IO(new MatrixAddition2IO(M, N))

  io.in.ready := false.B

  io.out.bits := DontCare
  io.out.valid := false.B

  private val sLoad :: sCompute :: sStore :: Nil = Enum(3)

  private val state = RegInit(sLoad)

  val regA = Reg(SInt(32.W))
  val regB = Reg(SInt(32.W))
  val regOut = Reg(Vec(M * N, SInt(32.W)))

  val i = Counter(M)
  val j = Counter(N)

  switch(state) {
    is(sLoad) {
      io.in.ready := true.B

      when(io.in.fire()) {
        regA := io.in.bits.a
        regB := io.in.bits.b
        state := sCompute
      }
    }
    is(sCompute) {
      var sum = 0.S(32.W)

      sum = regA + regB

      regOut(i.value * N.U + j.value) := sum

      state := sLoad

      when(j.inc()) {
        when(i.inc()) {
          state := sStore
        }
      }
    }
    is(sStore) {
      io.out.bits := regOut
      io.out.valid := true.B
    }
  }

  chisel3.printf(
    p"[$currentCycle] state: $state\n"
  )
}

object MatrixAddition2 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new MatrixAddition2(3, 2))
    )
  )
}