## 教程：使用 Chisel 实现马尔科夫预取器

### 介绍

在本教程中，您将学习如何使用 Chisel 实现一个马尔科夫预取器。Chisel 是一种嵌入在 Scala 中的硬件描述语言。马尔科夫预取器是一种硬件模块，旨在根据先前访问的历史预测未来的内存访问，通过预取即将需要的数据来提高内存系统的性能。

完成本教程后，您应该能够理解 Chisel 的基本概念，构建一个预取器模块，并使用 Chisel 的测试框架对其进行测试。

### 前提条件

- 了解数字设计和硬件描述语言的基本知识。
- 熟悉 Scala 编程语言。
- 了解内存系统和预取概念的基本知识。

### 设置

1. **安装 Chisel**：按照 [Chisel 安装指南](https://www.chisel-lang.org/getting-started.html) 设置您的环境。
2. **创建一个新的 Chisel 项目**：您可以使用 [Chisel 模板](https://github.com/freechipsproject/chisel-template) 来启动一个新项目。

### 分步实现

#### 步骤 1：定义访问历史条目

创建一个 `AccessHistoryEntry` 类来存储每次内存访问的信息。

```scala
package prefetcher

import chisel3._

// 存储每次内存访问信息的类
class AccessHistoryEntry extends Bundle {
  val address = UInt(5.W)       // 访问的地址
  val valid = Bool()            // 条目是否有效
  val prefetch = Bool()         // 是否为预取操作
  val timestamp = UInt(32.W)    // 记录访问的时间戳
}
```

#### 步骤 2：定义 IO 接口

为马尔科夫预取器模块定义输入和输出信号。

```scala
package prefetcher

import chisel3._

// 马尔科夫预取器的 IO 接口
class MarkovPrefetcherIO extends Bundle {
  val address = Input(UInt(5.W))                            // 当前访问的地址
  val prefetch = Output(Bool())                             // 是否需要进行预取
  val prefetchAddress = Output(UInt(5.W))                   // 预取的地址
  val hit = Output(Bool())                                  // 是否命中
  val prefetchHit = Output(Bool())                          // 是否命中预取
  val demandHit = Output(Bool())                            // 是否命中需求访问
  val accessHistory = Output(Vec(5, new AccessHistoryEntry)) // 访问历史记录
  val transitionTable = Output(Vec(32, Vec(32, UInt(8.W))))  // 状态转移表
  val currentAddress = Output(UInt(5.W))                    // 当前访问的地址
  val previousAddress = Output(UInt(5.W))                   // 上一次访问的地址
  val previousAddressValid = Output(Bool())                 // 上一次访问的地址是否有效
  val mostProbableNextAddress = Output(UInt(5.W))           // 最可能的下一个访问地址
  val mostProbableNextAddressValid = Output(Bool())         // 最可能的下一个访问地址是否有效
  val mostProbableNextAddressInHistory = Output(Bool())     // 最可能的下一个访问地址是否在历史记录中
  val cycleCounter = Output(UInt(32.W))                     // 周期计数器
  val fsmState = Output(UInt(3.W))                          // 有限状态机的状态
}
```

#### 步骤 3：实现马尔科夫预取器模块

创建主要的 `MarkovPrefetcher` 模块。该模块包含预取器的主要逻辑，包括状态机和马尔科夫预测逻辑。

```scala
package prefetcher

import chisel3._
import chisel3.util._


// 马尔科夫预取器的工具对象，包含一些实用函数
object MarkovPrefetcherUtils {
  // 在访问历史中查找指定地址，返回索引和是否存在的布尔值
  def findInAccessHistory(accessHistory: Vec[AccessHistoryEntry], address: UInt, addressValid: Bool): (UInt, Bool) = {
    val existsInHistory = accessHistory.exists(entry => entry.address === address && entry.valid && addressValid) // 检查地址是否存在于访问历史中
    val index = accessHistory.indexWhere(entry => entry.address === address && entry.valid && addressValid) // 找到地址在访问历史中的索引
    (index, existsInHistory) // 返回索引和是否存在的布尔值
  }

  // 查找在状态转移表中最可能的下一个访问地址，返回地址和是否有效的布尔值
  def findMostProbableNextAddress(transitions: Vec[UInt], addressValid: Bool): (UInt, Bool) = {
    val numAddresses = transitions.size // 获取状态转移表的大小
    var mostProbableNextAddress = 0.U(5.W) // 初始化最可能的下一个访问地址
    var maxCount = 0.U(8.W) // 初始化最大计数
    var mostProbableNextAddressValid = false.B // 初始化最可能的下一个访问地址是否有效

    // 遍历状态转移表，找到计数最大（最可能）的下一个访问地址
    for (i <- 0 until numAddresses) {
      val count = transitions(i.U) // 获取当前地址的计数
      val isMostProbable = (count > maxCount) && addressValid // 判断是否为计数最大且地址有效
      maxCount = Mux(isMostProbable, count, maxCount) // 更新最大计数
      mostProbableNextAddress = Mux(isMostProbable, i.U, mostProbableNextAddress) // 更新最可能的下一个访问地址
      mostProbableNextAddressValid = Mux(isMostProbable, true.B, mostProbableNextAddressValid) // 更新最可能的下一个访问地址是否有效
    }
    (mostProbableNextAddress, mostProbableNextAddressValid) // 返回最可能的下一个访问地址和是否有效的布尔值
  }

  // 查找访问历史中最老的条目的索引
  def findOldestEntryIndex(accessHistory: Vec[AccessHistoryEntry]): UInt = {
    var oldestIndex = 0.U(3.W) // 初始化最老条目的索引
    var oldestTimestamp = accessHistory(0).timestamp // 初始化最老的时间戳
    // 遍历访问历史，找到时间戳最老的条目
    for (i <- 1 until accessHistory.length) {
      val isOlder = accessHistory(i).timestamp < oldestTimestamp // 判断当前条目是否比最老的条目更老
      oldestTimestamp = Mux(isOlder, accessHistory(i).timestamp, oldestTimestamp) // 更新最老的时间戳
      oldestIndex = Mux(isOlder, i.U, oldestIndex) // 更新最老条目的索引
    }
    oldestIndex // 返回最老条目的索引
  }
}

// 马尔科夫预取器模块
class MarkovPrefetcher extends Module {
  val io = IO(new MarkovPrefetcherIO)

  val numAddresses = 32 // 状态转移表的地址数量
  val historySize = 5 // 访问历史记录的大小

  val cycleCounterReg = RegInit(0.U(32.W)) // 周期计数器寄存器
  cycleCounterReg := cycleCounterReg + 1.U

  val transitionTableReg = RegInit(VecInit(Seq.fill(numAddresses)(VecInit(Seq.fill(numAddresses)(0.U(8.W)))))) // 状态转移表寄存器
  val accessHistoryReg = RegInit(VecInit(Seq.fill(historySize)(0.U.asTypeOf(new AccessHistoryEntry)))) // 访问历史记录寄存器

  // 有限状态机状态
  val sIdle :: sFindHit :: sUpdateHistory1 :: sFindMostProbable :: sUpdateHistory2 :: sReportResult :: Nil = Enum(6)
  val stateReg = RegInit(sIdle) // 状态寄存器，初始状态为 sIdle

  val currentAddressReg = Reg(UInt(5.W)) // 当前访问地址寄存器
  val previousAddressReg = Reg(UInt(5.W)) // 上一次访问地址寄存器
  val previousAddressValidReg = RegInit(false.B) // 上一次访问地址是否有效寄存器
  val hitReg = Reg(Bool()) // 命中寄存器
  val hitIndexReg = Reg(UInt(3.W)) // 命中索引寄存器
  val prefetchHitReg = Reg(Bool()) // 预取命中寄存器
  val demandHitReg = Reg(Bool()) // 需求命中寄存器
  val prefetchReg = Reg(Bool()) // 预取寄存器
  val prefetchAddressReg = Reg(UInt(5.W)) // 预取地址寄存器
  val mostProbableNextAddressReg = Reg(UInt(5.W)) // 最可能的下一个访问地址寄存器
  val mostProbableNextAddressValidReg = Reg(Bool()) // 最可能的下一个访问地址是否有效寄存器
  val mostProbableNextAddressInHistoryReg = Reg(Bool()) // 最可能的下一个访问地址是否在历史记录中寄存器

  switch(stateReg) {
    is(sIdle) {
      currentAddressReg := io.address // 获取当前传入的地址
      // TODO: 初始化相关的寄存器
      stateReg := sFindHit // 切换到寻找命中状态
    }

    is(sFindHit) {
      val (hitIndex, hit) = MarkovPrefetcherUtils.findInAccessHistory(accessHistoryReg, currentAddressReg, true.B) // 查找当前地址在访问历史中的索引和是否命中
      val prefetchHit = hit && accessHistoryReg(hitIndex).prefetch // 判断是否为预取命中
      val demandHit = hit && !accessHistoryReg(hitIndex).prefetch // 判断是否为需求命中
      // TODO: 更新相关的寄存器
      stateReg := sUpdateHistory1 // 切换到更新历史状态1
    }

    is(sUpdateHistory1) {
      when(hitReg) {
        // TODO: 更新命中条目的时间戳
      }.otherwise {
        // TODO: 替换历史中最老的条目
      }
      stateReg := sFindMostProbable
    }

    is(sFindMostProbable) {
      val (mostProbableNextAddress, mostProbableNextAddressValid) = MarkovPrefetcherUtils.findMostProbableNextAddress(transitionTableReg(currentAddressReg), true.B)
      val (mostProbableNextAddressInHistoryIndex, mostProbableNextAddressInHistory) = MarkovPrefetcherUtils.findInAccessHistory(accessHistoryReg, mostProbableNextAddress, mostProbableNextAddressValid)
      // TODO: 更新相关的寄存器
      stateReg := sUpdateHistory2 // 切换到更新历史状态2
    }

    is(sUpdateHistory2) {
      when(previousAddressValidReg) {
        // TODO: 更新状态转移表
      }
      when(prefetchReg) {
        // TODO: 用预取地址替换最老的条目
      }
      // TODO: 更新相关的寄存器
      stateReg := sReportResult // 切换到报告结果状态
    }

    is(sReportResult) {
      stateReg := sIdle // 切换到空闲状态
    }
  }

  // 将寄存器连接到 IO
  io.prefetch := prefetchReg
  io.prefetchAddress := prefetchAddressReg
  io.hit := hitReg
  io.prefetchHit := prefetchHitReg
  io.demandHit := demandHitReg
  io.accessHistory := accessHistoryReg
  io.transitionTable := transitionTableReg
  io.currentAddress := currentAddressReg
  io.previousAddress := previousAddressReg
  io.previousAddressValid := previousAddressValidReg
  io.mostProbableNextAddress := mostProbableNextAddressReg
  io.mostProbableNextAddressValid := mostProbableNextAddressValidReg
  io.mostProbableNextAddressInHistory := mostProbableNextAddressInHistoryReg
  io.cycleCounter := cycleCounterReg
  io.fsmState := stateReg
}
```

### 步骤 4：编写测试

使用 Chisel 的测试框架来测试您的马尔科夫预取器。在新的文件中定义测试用例。

```scala
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
```

### 步骤 5：运行测试

使用以下命令运行测试：

```sh
sbt test
```

### 参考实现

为了验证您的 Chisel 设计，请使用以下马尔科夫预取器模拟器的 Scala 实现。此实现可以帮助您理解预取器的预期行为，并与您的 Chisel 设计进行比较。

```scala
package prefetcher

import scala.collection.mutable

object MarkovPrefetcherSimulator {

  // 定义预取事件类，包含地址、命中信息、预取地址和访问历史记录等字段
  case class PrefetchEvent(
    address: Int, // 访问的地址
    hit: Boolean, // 是否命中
    prefetchHit: Boolean, // 是否命中预取
    demandHit: Boolean, // 是否命中需求访问
    prefetch: Boolean, // 是否进行了预取
    prefetchAddress: Option[Int], // 预取的地址
    accessHistory: List[(Int, String)] // 访问历史记录
  )

  // 获取最可能的下一个地址
  def getMostProbableNextAddress(transitionTable: Array[Array[Int]], address: Int): Option[Int] = {
    // 找到当前地址行中转移次数最多的地址
    val maxTransitions = transitionTable(address).max
    // 如果最大转移次数为0，则返回None，否则返回对应的下一个地址
    if (maxTransitions == 0) None else Some(transitionTable(address).indexOf(maxTransitions))
  }

  // 模拟Markov预取器
  def simulatePrefetcher(numAddresses: Int, addresses: List[Int], historyWindowSize: Int = 5): List[PrefetchEvent] = {
    // 初始化转移表，每个元素初始值为0
    val transitionTable = Array.fill(numAddresses, numAddresses)(0)
    // 初始化访问历史记录队列，存储最近访问的地址及其访问类型
    val accessHistory = mutable.Queue[(Int, String)]()
    // 上一个访问的地址，初始为None
    var prevAddress: Option[Int] = None
    // 事件列表，存储每次访问的详细信息
    var events = List.empty[PrefetchEvent]

    // 遍历每个访问的地址
    addresses.foreach { address =>
      var hit = false // 是否命中
      var prefetchHit = false // 是否命中预取
      var demandHit = false // 是否命中需求访问
      var prefetch = false // 是否进行了预取
      var prefetchAddress: Option[Int] = None // 预取的地址

      // 检查当前地址是否在访问历史记录中
      accessHistory.zipWithIndex.foreach { case ((histAddress, accessType), i) =>
        // 如果当前访问地址在历史记录中找到
        if (address == histAddress) {
          hit = true // 表示命中
          // 如果命中且是预取类型，将其标记为非预取
          if (accessType == "Prefetch") {
            prefetchHit = true // 预取命中
            accessHistory(i) = (histAddress, "Demand") // 更新访问类型为需求
          } else {
            demandHit = true // 需求访问命中
          }
        }
      }

      // 如果未命中且有上一个地址，更新转移表
      if (!hit && prevAddress.isDefined) {
        transitionTable(prevAddress.get)(address) += 1 // 增加转移次数
      }

      // 更新访问历史记录，删除已经存在的地址
      accessHistory.dequeueAll(_._1 == address)
      // 将当前访问地址加入队列，标记为需求访问
      accessHistory.enqueue((address, "Demand"))

      // 如果访问历史记录超出窗口大小，移除最旧的记录
      if (accessHistory.size > historyWindowSize) {
        accessHistory.dequeue()
      }

      // 进行预取
      val predictedAddress = getMostProbableNextAddress(transitionTable, address)
      // 如果预测的地址存在且不在访问历史记录中，则进行预取
      if (predictedAddress.isDefined && !accessHistory.exists(_._1 == predictedAddress.get)) {
        // 将预测的地址加入访问历史记录队列，并标记为预取访问
        accessHistory.enqueue((predictedAddress.get, "Prefetch"))
        prefetch = true // 标记为进行了预取
        prefetchAddress = predictedAddress // 记录预取地址

        // 如果访问历史记录超出窗口大小，移除最旧的记录
        if (accessHistory.size > historyWindowSize) {
          accessHistory.dequeue()
        }
      }

      // 更新前一个访问地址为当前地址
      prevAddress = Some(address)
      // 创建预取事件对象并加入事件列表
      events = events :+ PrefetchEvent(
        address, // 当前访问地址
        hit, // 是否命中
        prefetchHit, // 是否命中预取
        demandHit, // 是否命中需求访问
        prefetch, // 是否进行了预取
        prefetchAddress, // 预取的地址
        accessHistory.toList // 当前的访问历史记录
      )
      
      // 调试输出：
      println(s"Address: $address")
      println(s"  - Current Address: $address")
      println(s"  - Previous Address: ${prevAddress.getOrElse("None")}")
      println(s"  - Hit: $hit")
      println(s"  - Prefetch Hit: $prefetchHit")
      println(s"  - Demand Hit: $demandHit")
      println(s"  - Prefetch: $prefetch")
      println(s"  - Prefetch Address: ${prefetchAddress.getOrElse("None")}")
      printAccessHistory(accessHistory)
      printTransitionTable(transitionTable)
    }

    events
  }

  // 打印访问历史记录
  def printAccessHistory(accessHistory: mutable.Queue[(Int, String)]): Unit = {
    val accessHistoryStr = accessHistory.map { case (address, accessType) =>
      s"($address, '$accessType')"
    }.mkString(", ")
    println(s"  - Access History: [$accessHistoryStr]")
  }

  // 打印转移表
  def printTransitionTable(transitionTable: Array[Array[Int]]): Unit = {
    val transitionTableStr = transitionTable.zipWithIndex.map { case (row, i) =>
      val entries = row.zipWithIndex.map { case (count, j) =>
        if (count > 0) s"$j($count)" else ""
      }.filter(_.nonEmpty).mkString(", ")
      if (entries.nonEmpty) s"$i -> [$entries]" else ""
    }.filter(_.nonEmpty).mkString(", ")
    println(s"  - Transition Table: $transitionTableStr")
  }
}
```

### 结论

在本教程中，您学习了如何使用 Chisel 实现一个马尔科夫预取器。您定义了必要的类和模块，实施了预取器的主要逻辑，并编写测试来验证其功能。通过这个练习，您应该对如何使用 Chisel 构建和测试硬件组件有了扎实的理解。