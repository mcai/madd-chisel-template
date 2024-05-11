package cache

import chisel3._
import chisel3.util._

class Request extends Bundle with CacheConfig {
  val address = UInt(addressWidth.W)
  val writeEnable = Bool()
  val writeData = UInt(dataWidth.W)
}

class Response extends Bundle with CacheConfig {
  val readData = UInt(dataWidth.W)
}

class CacheIO extends Bundle with CacheConfig {
  val request = Flipped(Decoupled(new Request))
  val response = Decoupled(new Response)

  val numHits = Output(UInt(32.W))
  val numCycles = Output(UInt(32.W))
}