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