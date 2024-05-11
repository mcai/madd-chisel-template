package cache

import chisel3._
import chisel3.util._

class MemoryIO(width: Int, depth: Int) extends Bundle {
    val writeEnable = Input(Bool())
    val address = Input(UInt(log2Ceil(depth).W))
    val writeData  = Input(UInt(width.W))
    val readData = Output(UInt(width.W))
  
    override def cloneType = new MemoryIO(width, depth).asInstanceOf[this.type]
}