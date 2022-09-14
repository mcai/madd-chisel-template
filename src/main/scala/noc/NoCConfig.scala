package noc

import chisel3._
import chisel3.util._

import scala.collection.mutable

class NeighborEntry(val config: NoCConfig) extends Bundle {
  val valid = Bool()
  val dest = UInt(log2Ceil(config.numRouters).W)
}

class NoCConfig {
  var numRouters = 16

  def width = math.sqrt(numRouters).toInt

  def diameter = (width - 1) * 2

  def getX(routerId: Int) = routerId % width

  def getY(routerId: Int) = routerId / width

  def getX(routerId: UInt) = routerId % width.U

  def getY(routerId: UInt) = routerId / width.U

  def neighbors(routerId: Int): mutable.Map[Int, Int] = {
    val (x, y) = (getX(routerId), getY(routerId))

    val neighbors = mutable.Map[Int, Int]()

    if (y > 0) {
      neighbors(Direction.north) = routerId - width
    }

    if (x < width - 1) {
      neighbors(Direction.east) = routerId + 1
    }

    if (y < width - 1) {
      neighbors(Direction.south) = routerId + width
    }

    if (x > 0) {
      neighbors(Direction.west) = routerId - 1
    }

    neighbors
  }

  def neighbors(routerId: UInt): Vec[NeighborEntry] = {
    val (x, y) = (getX(routerId), getY(routerId))

    val result = Wire(Vec(Direction.size, new NeighborEntry(this)))

    (0 until Direction.size).foreach { i =>
      result(i).valid := false.B
      result(i).dest := DontCare
    }

    val neighborsVerify: mutable.Map[Int, Int] = neighbors(routerId.litValue().toInt)

    when (y > 0.U) {
      result(Direction.north).valid := true.B
      result(Direction.north).dest := routerId - width.U
    }

    when (x < (width - 1).U) {
      result(Direction.east).valid := true.B
      result(Direction.east).dest := routerId + 1.U
    }

    when (y < (width - 1).U) {
      result(Direction.south).valid := true.B
      result(Direction.south).dest := routerId + width.U
    }

    when (x > 0.U) {
      result(Direction.west).valid := true.B
      result(Direction.west).dest := routerId - 1.U
    }

    (0 until Direction.size).foreach { i =>
      if(neighborsVerify.contains(i)) {
        assert(result(i).valid && result(i).dest === neighborsVerify(i).U, "[NoCConfig] neighbors(%d): result(%d).dest = %d, neighborsVerify(%d) = %d", routerId, i.U, result(i).dest, i.U, neighborsVerify(i).U)
      } else {
        assert(!result(i).valid)
      }
    }

    result
  }

  var routing = RoutingType.OddEven
  var selection = SelectionType.First

  var maxInputBufferSize = 2

  val numVirtualChannels = 2

  var dataPacketTraffic = TrafficType.Transpose1
  var dataPacketInjectionRate = 0.1
  var dataPacketSize = 16
}

object NoCConfig {
  def apply() = new NoCConfig()
}
