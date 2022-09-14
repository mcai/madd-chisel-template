package noc

import chisel3._
import chisel3.util._
import common.Demux
import chisel3.stage.ChiselStage
import chisel3.stage.ChiselGeneratorAnnotation

class InputBuffer(
    val config: NoCConfig,
    val payloadIndexBits: Int,
    val payloadDataBits: Int
) extends Module {
  val io = IO(
    new QueueIO(
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      ),
      config.maxInputBufferSize * config.numVirtualChannels
    )
  )

  private val virtualChannels = VecInit(
    Seq.fill(config.numVirtualChannels)(
      Module(
        new Queue(
          new Packet(
            config,
            payloadIndexBits,
            payloadDataBits
          ),
          config.maxInputBufferSize
        )
      ).io
    )
  )

  private val inDemux = Module(
    new Demux(
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      ),
      config.numVirtualChannels
    )
  )

  private val outArbiter = Module(
    new RRArbiter(
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      ),
      config.numVirtualChannels
    )
  )

  assert(config.numVirtualChannels == 2)

  inDemux.io.in <> io.enq
  inDemux.io.select := true.B
  inDemux.io.out <> virtualChannels.map(_.enq)

  outArbiter.io.in <> virtualChannels.map(_.deq)
  io.deq <> outArbiter.io.out

  io.count := virtualChannels.map(_.count).reduce(_ + _)
}

object InputBuffer extends App {
  private val config = NoCConfig()

  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new InputBuffer(config, 8, 16))
    )
  )
}
