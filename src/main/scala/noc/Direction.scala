package noc

import chisel3._
import chisel3.util._

object Direction {
  val local = 0
  val north = 1
  val east = 2
  val south = 3
  val west = 4

  val size = 5

  def opposite(direction: Int) = direction match {
    case `local` => local
    case `north` => south
    case `east` => west
    case `south` => north
    case _ => east
  }
}