package noc

import chisel3._
import chisel3.util._
import common.CurrentCycle
import _root_.circt.stage.ChiselStage

class Router(
    val config: NoCConfig,
    val id: Int,
    val payloadIndexBits: Int,
    val payloadDataBits: Int
) extends Module
    with CurrentCycle {
  val io = IO(
    new RouterIO(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val inputBuffers = VecInit(
    Seq.fill(Direction.size)(
      Module(
        new InputBuffer(
          config,
          payloadIndexBits,
          payloadDataBits
        )
      ).io
    )
  )

  private val routeComputation = new RouteComputation(config, id)

  private val arbiters = VecInit(
    Seq.fill(Direction.size)(
      Module(new RRArbiter(UInt(log2Ceil(Direction.size).W), Direction.size)).io
    )
  )

  private val outs = Wire(
    Vec(
      Direction.size,
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      )
    )
  )
  private val dirOuts = Wire(
    Vec(Direction.size, UInt(log2Ceil(Direction.size).W))
  )

  (0 until Direction.size).foreach { inputDir =>
    inputBuffers(inputDir).enq <> io.packetIn(inputDir)
    inputBuffers(inputDir).deq.ready := arbiters.exists(arbiter =>
      arbiter.out.valid && arbiter.out.bits === inputDir.U && arbiter
        .in(inputDir)
        .ready
    )

    when(inputBuffers(inputDir).deq.valid) {
      routeComputation(
        inputBuffers(inputDir).deq.bits,
        inputDir,
        outs(inputDir),
        dirOuts(inputDir),
        io.numFreeSlotsIn
      )
    }.otherwise {
      outs(inputDir) := DontCare
      dirOuts(inputDir) := 0.U
    }

    io.numFreeSlotsOut(inputDir) := config.maxInputBufferSize.U - inputBuffers(
      inputDir
    ).count
  }

  (0 until Direction.size).foreach { outputDir =>
    (0 until Direction.size).foreach { inputDir =>
      arbiters(outputDir).in(inputDir).valid := inputBuffers(
        inputDir
      ).deq.valid && dirOuts(inputDir) === outputDir.U
      arbiters(outputDir).in(inputDir).bits := inputDir.U
    }
  }

  (0 until Direction.size).foreach { outputDir =>
    io.packetOut(outputDir).valid := arbiters(outputDir).out.valid
    io.packetOut(outputDir).bits := outs(arbiters(outputDir).out.bits)
    arbiters(outputDir).out.ready := io.packetOut(outputDir).ready
  }

  (0 until Direction.size).foreach { i =>
    when(io.packetIn(i).fire) {
      chisel3.printf(
        p"[$currentCycle Router#$id.in#$i] Received: ${io.packetIn(i).bits}\n"
      )
    }
    when(io.packetOut(i).fire) {
      chisel3.printf(
        p"[$currentCycle Router#$id.out#$i] Sent: ${io.packetOut(i).bits}\n"
      )
    }
  }
}

object Router extends App {
  private val config = NoCConfig()
  ChiselStage.emitSystemVerilogFile(new Router(config, 0, 8, 16))
}