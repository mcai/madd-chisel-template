package noc

import chisel3._
import chisel3.util._
import common.CurrentCycle
import noc.routing.{XYRoutingAlgorithm, NegativeFirstRoutingAlgorithm, WestFirstRoutingAlgorithm, NorthLastRoutingAlgorithm, OddEvenRoutingAlgorithm}

class RouteComputation(val config: NoCConfig, val routerId: Int)
    extends CurrentCycle {
  private val neighbors =
    config.neighbors(routerId.U(log2Ceil(config.numRouters).W))

  def apply(
      in: Packet,
      dirIn: Int,
      out: Packet,
      dirOut: UInt,
      numFreeSlotsIn: Vec[UInt]
  ) = {
    val routing = config.routing match {
      case RoutingType.XY => Module(new XYRoutingAlgorithm(config, routerId))
      case RoutingType.NegativeFirst =>
        Module(new NegativeFirstRoutingAlgorithm(config, routerId))
      case RoutingType.WestFirst =>
        Module(new WestFirstRoutingAlgorithm(config, routerId))
      case RoutingType.NorthLast =>
        Module(new NorthLastRoutingAlgorithm(config, routerId))
      case RoutingType.OddEven =>
        Module(new OddEvenRoutingAlgorithm(config, routerId))
      case _ => ???
    }

    routing.io.current := routerId.U
    routing.io.src := in.src
    routing.io.dest := in.dest

    out := in

    val destArrived = in.dest === routerId.U

    when(destArrived) {
      memorize()
      dirOut := Direction.local.U
    }.otherwise {
      memorize()
      forwardPacket()
    }

    chisel3.printf(
      p"[$currentCycle Router#$routerId.routeComputation] Received: dirIn = $dirIn, in = $in\n"
    )

    chisel3.printf(
      p"[$currentCycle Router#$routerId.routeComputation] Sent: dirOut = $dirOut, out = $out\n"
    )

    def forwardPacket() = {
      chisel3.printf(
        p"[$currentCycle Router#$routerId.routeComputation] forwardPacket\n"
      )

      config.selection match {
        case SelectionType.First => forwardPacketByFirstSelection()
        case _                   => ???
      }
    }

    def forwardPacketByFirstSelection() = {
      chisel3.printf(
        p"[$currentCycle Router#$routerId.routeComputation] forwardPacketByFirstSelection\n"
      )

      val found: Bool = routing.io.dirs.exists(_ === true.B)

      assert(found)

      dirOut := routing.io.dirs.indexWhere(_ === true.B)
    }

    def memorize() = {
      chisel3.printf(
        p"[$currentCycle Router#$routerId.routeComputation] memorize\n"
      )

      assert(
        in.numMemories < in.memories.size.U,
        "in.numMemories: %d, in.memories.size: %d",
        in.numMemories,
        in.memories.size.U
      )

      out.memories(in.numMemories) := routerId.U
      out.numMemories := in.numMemories + 1.U
    }
  }
}
