package madd

import chisel3._
import chisel3.util._

class MatrixAddition3IO(M: Int, N: Int) extends Bundle {
  val start = Input(Bool())
  val load = Output(Bool())
  val done = Output(Bool())

  val k = Output(UInt(32.W))

  val a = Input(SInt(32.W))
  val b = Input(SInt(32.W))

  val out = Output(SInt(32.W))

  override def cloneType =
    new MatrixAddition3IO(M, N).asInstanceOf[this.type]
}