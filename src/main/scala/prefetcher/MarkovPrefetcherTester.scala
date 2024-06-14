package prefetcher

import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

// 打印工具对象，包含打印访问历史和状态转移表的函数
object PrintUtils {
  // 打印访问历史
  def printAccessHistory(dut: MarkovPrefetcher): Unit = {
    val accessHistory = dut.io.accessHistory.map { entry =>
      val address = entry.address.peek().litValue // 获取地址值
      val prefetchType = if (entry.prefetch.peek().litToBoolean) "prefetch" else "demand" // 判断是预取还是需求
      val valid = entry.valid.peek().litToBoolean // 获取条目是否有效
      val timestamp = entry.timestamp.peek().litValue // 获取时间戳
      (address, prefetchType, valid, timestamp) // 返回访问历史条目信息的元组
    }

    val sortedAccessHistory = accessHistory.filter(_._3).sortBy(_._4) // 过滤有效条目并按时间戳排序

    val accessHistoryStr = sortedAccessHistory.map { case (address, prefetchType, _, timestamp) =>
      s"($address, '$prefetchType', $timestamp)" // 格式化访问历史条目为字符串
    }.mkString(", ")

    println(s"  - Access history: [$accessHistoryStr]") // 打印访问历史
  }

  // 打印状态转移表
  def printTransitionTable(dut: MarkovPrefetcher): Unit = {
    val transitionTableStr = (0 until 32).map { i =>
      val entries = dut.io.transitionTable(i).zipWithIndex.map { case (count, idx) =>
        if (count.peek().litValue > 0) s"$idx(${count.peek().litValue})" else "" // 获取非零计数的转移条目
      }.filter(_.nonEmpty).mkString(", ")
      if (entries.nonEmpty) s"$i -> [$entries]" else "" // 格式化转移条目为字符串
    }.filter(_.nonEmpty).mkString(", ")
    println(s"  - Transition table: $transitionTableStr") // 打印状态转移表
  }

  // 打印DUT（Device Under Test）的输出
  def printDutOutputs(dut: MarkovPrefetcher, address: Int, cycle: Long, state: String): Unit = {
    println(s"[$cycle] State: $state, Address: $address")
    println(s"  - Current Address: ${dut.io.currentAddress.peek().litValue}")
    println(s"  - Previous Address: ${dut.io.previousAddress.peek().litValue}")
    println(s"  - Previous Address Valid: ${dut.io.previousAddressValid.peek().litToBoolean}")
    println(s"  - Hit: ${dut.io.hit.peek().litToBoolean}")
    println(s"  - Prefetch Hit: ${dut.io.prefetchHit.peek().litToBoolean}")
    println(s"  - Demand Hit: ${dut.io.demandHit.peek().litToBoolean}")
    println(s"  - Prefetch: ${dut.io.prefetch.peek().litToBoolean}")
    println(s"  - Prefetch Address: ${dut.io.prefetchAddress.peek().litValue}")
    println(s"  - Most Probable Next Address: ${dut.io.mostProbableNextAddress.peek().litValue}")
    println(s"  - Most Probable Next Address Valid: ${dut.io.mostProbableNextAddressValid.peek().litToBoolean}")
    println(s"  - Most Probable Next Address In History: ${dut.io.mostProbableNextAddressInHistory.peek().litToBoolean}")
    PrintUtils.printAccessHistory(dut) // 打印访问历史
    PrintUtils.printTransitionTable(dut) // 打印状态转移表
  }
}

// 马尔科夫预取器的测试类
class MarkovPrefetcherSpec extends AnyFreeSpec with Matchers {
  // 运行测试的方法
  def runTest(dut: MarkovPrefetcher, addresses: Seq[Int], expectedEvents: Seq[MarkovPrefetcherSimulator.PrefetchEvent]): Unit = {
    var hits = 0
    var prefetchHits = 0
    var demandHits = 0
    var prefetchRequests = 0

    val fsmStateToString = Map(
      0 -> "Idle",
      1 -> "FindHit",
      2 -> "UpdateHistory1",
      3 -> "FindMostProbable",
      4 -> "UpdateHistory2",
      5 -> "ReportResult"
    ) // 有限状态机状态到字符串的映射

    // 迭代测试地址和预期事件
    for ((address, event) <- addresses.zip(expectedEvents)) {
      val accessHistoryStr = event.accessHistory.map { case (address, accessType) =>
        s"($address, '$accessType')" // 格式化访问历史条目为字符串
      }.mkString(", ")
      println(s"(Scala) Event - Address: ${event.address}, Hit: ${event.hit}, Prefetch Hit: ${event.prefetchHit}, Demand Hit: ${event.demandHit}, Prefetch: ${event.prefetch}, Prefetch Address: ${event.prefetchAddress.getOrElse("None")}, Access History: [$accessHistoryStr]")

      dut.io.address.poke(address.U) // 将地址输入DUT

      // 迭代FSM的每个步骤
      (0 to 5).foreach { step =>
        val cycle = dut.io.cycleCounter.peek().litValue // 获取当前周期数
        val fsmState = dut.io.fsmState.peek().litValue // 获取当前FSM状态
        val stateStr = fsmStateToString(fsmState.toInt) // 将FSM状态转换为字符串

        PrintUtils.printDutOutputs(dut, address, cycle.toLong, stateStr) // 打印DUT的输出

        // 在最后一步检查预期结果
        if (step == 5) {
          if (dut.io.hit.peek().litToBoolean) hits += 1
          if (dut.io.prefetchHit.peek().litToBoolean) prefetchHits += 1
          if (dut.io.demandHit.peek().litToBoolean) demandHits += 1
          if (dut.io.prefetch.peek().litToBoolean) prefetchRequests += 1

          dut.io.hit.expect(event.hit.B, s"Hit check failed for address $address. Expected: ${event.hit}, Actual: ${dut.io.hit.peek().litToBoolean}")
          dut.io.prefetchHit.expect(event.prefetchHit.B, s"Prefetch hit check failed for address $address. Expected: ${event.prefetchHit}, Actual: ${dut.io.prefetchHit.peek().litToBoolean}")
          dut.io.demandHit.expect(event.demandHit.B, s"Demand hit check failed for address $address. Expected: ${event.demandHit}, Actual: ${dut.io.demandHit.peek().litToBoolean}")
          dut.io.prefetch.expect(event.prefetch.B, s"Prefetch check failed for address $address. Expected: ${event.prefetch}, Actual: ${dut.io.prefetch.peek().litToBoolean}")
          if (event.prefetchAddress.isDefined) {
            dut.io.prefetchAddress.expect(event.prefetchAddress.get.U, s"Prefetch address check failed for address $address. Expected: ${event.prefetchAddress.get}, Actual: ${dut.io.prefetchAddress.peek().litValue}")
          }
        }

        dut.clock.step(1) // 时钟周期前进一步
      }
    }

    println(f"\nHits: $hits, Prefetch Hits: $prefetchHits, Demand Hits: $demandHits, Prefetch Requests: $prefetchRequests") // 打印测试结果
  }

  // 测试实例
  "MarkovPrefetcher should predict next address based on various patterns" in {
    val patterns = Seq(
      ("Sequential pattern", Seq(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)), // 顺序模式
      ("Strided pattern", Seq(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)), // 跨步模式
      ("Interleaved pattern", Seq(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).grouped(2).flatMap(_.reverse).toSeq), // 交错模式
      ("Random pattern", Seq.fill(10)(scala.util.Random.nextInt(32))), // 随机模式
      ("Repeated pattern", Seq(0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5)) // 重复模式
    )

    // 遍历每种模式进行测试
    patterns.foreach { case (patternName, addresses) =>
      println(s"\n$patternName:")
      println(s"\n  - ${addresses.mkString(", ")}")

      simulate(new MarkovPrefetcher()) { dut =>
        val expectedEvents = MarkovPrefetcherSimulator.simulatePrefetcher(32, addresses.toList) // 生成预期事件
        runTest(dut, addresses, expectedEvents) // 运行测试
      }
    }
  }
}

// MarkovPrefetcher 测试器主程序
object MarkovPrefetcherTester extends App {
  (new MarkovPrefetcherSpec).execute() // 执行测试
}