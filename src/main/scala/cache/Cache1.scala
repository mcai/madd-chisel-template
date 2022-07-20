package cache

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import common.CurrentCycle

class Meta extends Bundle with CacheConfig {
  val valid = Bool()
  val dirty = Bool()
  val address = UInt(addressWidth.W)
  val tag = UInt(tagBits.W)
  val timestamp = UInt(32.W)
}

class Cache1 extends Module with CacheConfig with CurrentCycle {
  scala.Predef.printf(s"indexBits: ${indexBits}, offsetBits: ${offsetBits}\n")

  val io = IO(new CacheIO)

  val dataArray = RegInit(VecInit(Seq.fill(assoc * numSets)(0.U((blockSizeInBytes * 8).W))))
  val metaArray = RegInit(VecInit(Seq.fill(assoc * numSets)(
    {
      val meta = Wire(new Meta())
      meta.valid := false.B
      meta.dirty := false.B
      meta.address := 0.U
      meta.tag := 0.U
      meta.timestamp := 0.U
      meta
    }
  )))

  val sIdle :: sReadMiss :: sReadData :: sWriteResponse :: Nil = Enum(4)

  val regState = RegInit(sIdle)

  io.request.ready := false.B

  io.response.valid := false.B
  io.response.bits := DontCare

  val address = io.request.bits.address

  val tag = getTag(address)
  val index = getIndex(address)

  val regNumHits = RegInit(0.U(32.W))

  io.numHits := regNumHits

  io.numCycles := currentCycle

  val addressReg = Reg(UInt(addressWidth.W))
  val tagReg = getTag(addressReg)
  val indexReg = getIndex(addressReg)

  val wayReg = RegInit(assoc.U)

  val memory = Module(new Memory(dataWidth, 256))

  memory.io.writeEnable := false.B
  memory.io.address := DontCare
  memory.io.writeData := DontCare

  def fullIndex(index: UInt, way: UInt): UInt = assoc.U * index + way

  def lookup(index: UInt, tag: UInt): UInt = {
    var way = assoc.U

    for (i <- 0 until assoc) {
      var meta = metaArray(fullIndex(index, i.U))
      way = Mux(meta.valid && meta.tag === tag, i.U, way)
    }

    way
  }

  def findInvalidWay(index: UInt): UInt = {
    var way = assoc.U

    for (i <- 0 until assoc) {
      var meta = metaArray(fullIndex(index, i.U))
      way = Mux(way === assoc.U && !meta.valid, i.U, way)
    }

    way
  }

  def findVictimWay(index: UInt): UInt = {
    var way = assoc.U
    var minTimestamp = currentCycle

    for (i <- 0 until assoc) {
      var meta = metaArray(fullIndex(index, i.U))
      var found = meta.timestamp < minTimestamp
      minTimestamp = Mux(found, meta.timestamp, minTimestamp)
      way = Mux(found, i.U, way)
    }

    way
  }

  def writeback(index: UInt, way: UInt): Unit = {
    var fi = fullIndex(index, way)

    memory.io.writeEnable := true.B
    memory.io.address := metaArray(fi).address
    memory.io.writeData := dataArray(fi)
  }

  def doWrite(index: UInt, way: UInt): Unit = {
    var fi = fullIndex(index, way)

    metaArray(fi).valid := true.B
    metaArray(fi).dirty := true.B
    metaArray(fi).tag := tag
    metaArray(fi).address := address
    metaArray(fi).timestamp := currentCycle
    dataArray(fi) := io.request.bits.writeData
  }

  def doRead(index: UInt, way: UInt): Unit = {
    var fi = fullIndex(index, way)

    metaArray(fi).valid := true.B
    metaArray(fi).dirty := false.B
    metaArray(fi).tag := tagReg
    metaArray(fi).address := addressReg
    metaArray(fi).timestamp := currentCycle
    dataArray(fi) := memory.io.readData
  }

  def refill(): Unit = {
      memory.io.writeEnable := false.B
      memory.io.address := io.request.bits.address
  }

  switch(regState) {
    is(sIdle) {
      io.request.ready := true.B

      when(io.request.fire()) {
        addressReg := io.request.bits.address

        var way = lookup(index, tag)
        var hit = way =/= assoc.U

        when(io.request.bits.writeEnable) {
          when(hit) {
            regNumHits := regNumHits + 1.U

            var fi = fullIndex(index, way)

            metaArray(fi).timestamp := currentCycle
            dataArray(fi) := io.request.bits.writeData

            regState := sWriteResponse
          }.otherwise {
            var invalidWay = findInvalidWay(index)

            when(invalidWay === assoc.U) {
              var victimWay = findVictimWay(index)
              
              var victimMeta = metaArray(fullIndex(index, victimWay))
              
              when(victimMeta.dirty) {
                writeback(index, victimWay)
              }

              doWrite(index, victimWay)
            }.otherwise {
              doWrite(index, invalidWay)
            }

            regState := sWriteResponse
          }
        }.otherwise {
          when(hit) {
            regNumHits := regNumHits + 1.U

            var fi = fullIndex(index, way)

            metaArray(fi).timestamp := currentCycle
            
            regState := sReadData
          }.otherwise {
            var invalidWay = findInvalidWay(index)

            when(invalidWay === assoc.U) {
              var victimWay = findVictimWay(index)
              var victimMeta = metaArray(fullIndex(index, victimWay))
              
              when(victimMeta.dirty) {
                writeback(index, victimWay)
              }

              wayReg := victimWay
            }.otherwise {
              wayReg := invalidWay
            }

            refill()

            regState := sReadMiss
          }
        }
      }
    }
    is(sReadMiss) {
      doRead(indexReg, wayReg)

      regState := sReadData
    }
    is(sReadData) {
      var fi = fullIndex(indexReg, wayReg)

      io.response.valid := true.B
      io.response.bits.readData := dataArray(fi)

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

  chisel3.printf(
    p"[$currentCycle] regState: ${regState}, request.fire(): ${io.request.fire()}, response.fire(): ${io.response.fire()}, writeEnable: ${io.request.bits.writeEnable}, address: ${io.request.bits.address}, tag: ${tag}, index: ${index}, regNumHits: ${regNumHits}\n"
  )
}

object Cache1 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new Cache1())
    )
  )
}