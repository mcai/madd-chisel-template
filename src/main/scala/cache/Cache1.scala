package cache

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import common.CurrentCycle

class Meta extends Bundle with CacheConfig {
  val valid = Bool()
  val address = UInt(addressWidth.W)
  val tag = UInt(tagBits.W)
}

class Cache1 extends Module with CacheConfig {
    assert(assoc == 1)

    val io = IO(new CacheIO)

    val dataArray = Reg(Vec(numSets, UInt((blockSizeInBytes * 8).W)))
    val metaArray = Reg(Vec(numSets, new Meta()))

    val sIdle :: sReadMiss :: sReadData :: sWriteResponse :: Nil = Enum(4)

    val regState = RegInit(sIdle)

    io.request.ready := false.B

    io.response.valid := false.B
    io.response.bits := DontCare

    val address = io.request.bits.address

    val tag = address(addressWidth - 1, indexBits + offsetBits)
    val index = address(indexBits + offsetBits - 1, offsetBits)
    val offset = address(offsetBits - 1, 0)

    val hit = metaArray(index).tag === tag

    val memory = Module(new Memory(dataWidth, 256))

    memory.io.writeEnable := false.B
    memory.io.address := DontCare
    memory.io.writeData := DontCare

    def writeback() {
        memory.io.writeEnable := true.B
        memory.io.address := metaArray(index).address
        memory.io.writeData := dataArray(index)
    }

    def refill() {
        memory.io.writeEnable := false.B
        memory.io.address := io.request.bits.address
    }

    switch(regState) {
      is(sIdle) {
        io.request.ready := true.B

        when(io.request.fire()) {
          when(io.request.bits.writeEnable) {
            when(hit) {
              dataArray(index) := io.request.bits.writeData

              regState := sWriteResponse
            }.otherwise {
              when(metaArray(index).valid) {
                writeback()
              }

              metaArray(index).valid := true.B
              metaArray(index).tag := tag
              metaArray(index).address := address
              dataArray(index) := io.request.bits.writeData

              regState := sWriteResponse
            }
          }.otherwise {
            when(hit) {
              regState := sReadData
            }.otherwise {
              when(metaArray(index).valid) {
                writeback()
              }

              refill()

              regState := sReadMiss
            }
          }
        }
      }
      is(sReadMiss) {
        metaArray(index).valid := true.B
        metaArray(index).tag := tag
        metaArray(index).address := address
        dataArray(index) := memory.io.readData

        regState := sReadData
      }
      is(sReadData) {
        io.response.valid := true.B
        io.response.bits.readData := dataArray(index)

        when(io.response.fire()) {
          regState := sIdle
        }
      }
      is(sWriteResponse) {
        io.response.valid := true.B

        when(io.response.fire()) {
          regState := sIdle
        }
      }
    }
}

object Cache1 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new Cache1())
    )
  )
}