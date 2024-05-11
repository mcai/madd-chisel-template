package noc

import common.{CurrentCycle, Demux}
import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class NoCSimulator(
    val config: NoCConfig,
    val payloadIndexBits: Int,
    val payloadDataBits: Int
) extends Module
    with CurrentCycle {
  val io = IO(
    new NoCSimulatorIO(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val network = Module(
    new Network(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val inDemux = Module(
    new Demux(
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      ),
      config.numRouters
    )
  )

  private val outArbiter = Module(
    new RRArbiter(
      new Packet(
        config,
        payloadIndexBits,
        payloadDataBits
      ),
      config.numRouters
    )
  )

  inDemux.io.in <> io.packetIn
  inDemux.io.select := io.packetIn.bits.src
  network.io.packetIn <> inDemux.io.out

  outArbiter.io.in <> network.io.packetOut
  io.packetOut <> outArbiter.io.out

  private val (numDataPacketsReceived, _) =
    Counter(io.packetIn.fire, Int.MaxValue)
  private val (numDataPacketsSent, _) =
    Counter(io.packetOut.fire, Int.MaxValue)

  io.numDataPacketsReceived := numDataPacketsReceived
  io.numDataPacketsSent := numDataPacketsSent

  when(io.packetIn.fire) {
    chisel3.printf(
      p"[$currentCycle NoCSimulator] Received: ${io.packetIn.bits}\n"
    )
  }

  when(io.packetOut.fire) {
    chisel3.printf(
      p"[$currentCycle NoCSimulator] Sent: ${io.packetOut.bits}\n"
    )
  }

  when(io.packetIn.fire || io.packetOut.fire) {
    chisel3.printf(
      p"[$currentCycle NoCSimulator] numDataPacketsReceived = $numDataPacketsReceived, numDataPacketsSent = $numDataPacketsSent\n"
    )
  }
}

object NoCSimulator extends App {
  private val config = NoCConfig()
  ChiselStage.emitSystemVerilogFile(new NoCSimulator(config, 8, 16))
}