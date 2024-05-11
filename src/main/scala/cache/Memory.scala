package cache

import chisel3._
import chisel3.util._
import common.CurrentCycle

class Memory(width: Int, depth: Int) extends Module {
    val io = IO(new MemoryIO(width, depth))

    val mem = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))

    io.readData := RegNext(mem(io.address % depth.U))

    when(io.writeEnable) {
        mem(io.address % depth.U) := io.writeData
    }
}