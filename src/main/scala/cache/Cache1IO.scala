package cache

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class Cache1IO() extends Bundle {
  // TODO
  
  override def cloneType =
    new Cache1IO().asInstanceOf[this.type]
}