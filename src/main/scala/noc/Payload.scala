package noc

import chisel3._

class Payload(val config: NoCConfig, val indexBits: Int, val dataBits: Int) extends Bundle {
  val index = UInt(indexBits.W)
  val data = SInt(dataBits.W)

  override def cloneType = new Payload(config, indexBits, dataBits).asInstanceOf[this.type]
}
