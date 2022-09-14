package noc

import common.CurrentCycle
import chisel3._
import chisel3.util._
import noc.traffic.{
  Transpose1PacketGenerator,
  Transpose2PacketGenerator,
  UniformPacketGenerator
}

import scala.collection.mutable.ArrayBuffer
import chisel3.stage.ChiselStage
import chisel3.stage.ChiselGeneratorAnnotation

class NoCSimulatorTop(
    val config: NoCConfig,
    val maxPackets: Int,
    val payloadIndexBits: Int,
    val payloadDataBits: Int
) extends Module
    with CurrentCycle {
  val io = IO(
    new NoCSimulatorTopIO(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val simulator = Module(
    new NoCSimulator(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val packetGenerators = new ArrayBuffer[AbstractPacketGenerator]()

  packetGenerators += (config.dataPacketTraffic match {
    case TrafficType.Uniform =>
      Module(
        new UniformPacketGenerator(
          config,
          0,
          maxPackets,
          config.dataPacketInjectionRate,
          config.dataPacketSize,
          payloadIndexBits,
          payloadDataBits
        )
      )
    case TrafficType.Transpose1 =>
      Module(
        new Transpose1PacketGenerator(
          config,
          0,
          maxPackets,
          config.dataPacketInjectionRate,
          config.dataPacketSize,
          payloadIndexBits,
          payloadDataBits
        )
      )
    case TrafficType.Transpose2 =>
      Module(
        new Transpose2PacketGenerator(
          config,
          0,
          maxPackets,
          config.dataPacketInjectionRate,
          config.dataPacketSize,
          payloadIndexBits,
          payloadDataBits
        )
      )
    case _ => ???
  })

  if (packetGenerators.size > 1) {
    val inArbiter = Module(
      new RRArbiter(
        new Packet(
          config,
          payloadIndexBits,
          payloadDataBits
        ),
        packetGenerators.size
      )
    )

    inArbiter.io.in <> packetGenerators.map(_.io.packetOut)

    simulator.io.packetIn <> inArbiter.io.out
  } else {
    simulator.io.packetIn <> packetGenerators.head.io.packetOut
  }

  simulator.io.packetOut.ready := true.B

  io.currentCycle := currentCycle

  io.debugDataPacketIn.valid := simulator.io.packetIn.fire
  io.debugDataPacketIn.bits := simulator.io.packetIn.bits

  io.debugDataPacketOut.valid := simulator.io.packetOut.fire
  io.debugDataPacketOut.bits := simulator.io.packetOut.bits

  io.numDataPacketsReceived := simulator.io.numDataPacketsReceived
  io.numDataPacketsSent := simulator.io.numDataPacketsSent
}

object NoCSimulatorTop extends App {
  private val config = NoCConfig()

  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() =>
        new NoCSimulatorTop(config, 100, 8, 16)
      )
    )
  )
}
