package noc

import common.CurrentCycle
import chisel3.Module

abstract class RoutingAlgorithm(val config: NoCConfig) extends Module with CurrentCycle {
  val io = IO(new RoutingAlgorithmIO(config))

  protected val (x, y) = (config.getX(io.current), config.getY(io.current))
  protected val (srcX, srcY) = (config.getX(io.src), config.getY(io.src))
  protected val (destX, destY) = (config.getX(io.dest), config.getY(io.dest))
}
