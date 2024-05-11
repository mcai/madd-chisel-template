package noc

import common.CurrentCycle
import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// Example mesh topology (64 = 8 x 8):
//
//  0    1    2    3    4    5    6    7
//  8    9   10   11   12   13   14   15
// 16   17   18   19   20   21   22   23
// 24   25   26   27   28   29   30   31
// 32   33   34   35   36   37   38   39
// 40   41   42   43   44   45   46   47
// 48   49   50   51   52   53   54   55
// 56   57   58   59   60   61   62   63
//
// Example mesh topology (16 = 4 x 4):
//
//  0    1     2     3
//  4    5     6     7
//  8    9    10    11
// 12   13    14    15

class Network(
    val config: NoCConfig,
    val payloadIndexBits: Int,
    val payloadDataBits: Int
) extends Module
    with CurrentCycle {
  val io = IO(
    new NetworkIO(
      config,
      payloadIndexBits,
      payloadDataBits
    )
  )

  private val routers = VecInit(
    (0 until config.numRouters).map(i =>
      Module(
        new Router(
          config,
          i,
          payloadIndexBits,
          payloadDataBits
        )
      ).io
    )
  )

  (0 until config.numRouters).foreach { i =>
    routers(i).packetIn(Direction.local) <> io.packetIn(i)

    io.packetOut(i) <> routers(i).packetOut(Direction.local)

    routers(i).numFreeSlotsIn(Direction.local) := config.maxInputBufferSize.U

    (1 until Direction.size).foreach { j =>
      routers(i).packetIn(j).valid := false.B
      routers(i).packetIn(j).bits := DontCare
      routers(i).packetIn(j).bits.payload.data := 0.S

      routers(i).packetOut(j).ready := false.B

      routers(i).numFreeSlotsIn(j) := 0.U
    }
  }

  private def connectRouters(from: Int, to: Int, dir: Int) = {
    routers(from).packetIn(dir).valid := routers(to)
      .packetOut(Direction.opposite(dir))
      .valid
    routers(from).packetIn(dir).bits := routers(to)
      .packetOut(Direction.opposite(dir))
      .bits

    routers(to).packetOut(Direction.opposite(dir)).ready := routers(from)
      .packetIn(dir)
      .ready

    routers(from).numFreeSlotsIn(dir) := routers(to).numFreeSlotsOut(
      Direction.opposite(dir)
    )
  }

  (0 until config.numRouters).foreach { i =>
    val (x, y) = (config.getX(i), config.getY(i))

    if (y > 0) {
      connectRouters(i, i - config.width, Direction.north)
    }

    if (x < (config.width - 1)) {
      connectRouters(i, i + 1, Direction.east)
    }

    if (y < (config.width - 1)) {
      connectRouters(i, i + config.width, Direction.south)
    }

    if (x > 0) {
      connectRouters(i, i - 1, Direction.west)
    }

    when(io.packetIn(i).fire) {
      chisel3.printf(
        p"[$currentCycle Network.in(Router#$i)] Received: ${io.packetIn(i).bits}\n"
      )
    }

    when(io.packetOut(i).fire) {
      chisel3.printf(
        p"[$currentCycle Network.out(Router#$i)] Sent: ${io.packetOut(i).bits}\n"
      )
    }
  }
}

object Network extends App {
  private val config = NoCConfig()
  ChiselStage.emitSystemVerilogFile(new Network(config, 8, 16))
}