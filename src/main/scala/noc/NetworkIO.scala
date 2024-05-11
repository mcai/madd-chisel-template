package noc

import chisel3._
import chisel3.util._

class NetworkIO(val config: NoCConfig, val payloadIndexBits: Int, val payloadDataBits: Int) extends Bundle {
  val packetIn = Vec(config.numRouters, Flipped(Decoupled(new Packet(config, payloadIndexBits, payloadDataBits))))
  val packetOut = Vec(config.numRouters, Decoupled(new Packet(config, payloadIndexBits, payloadDataBits)))
}
