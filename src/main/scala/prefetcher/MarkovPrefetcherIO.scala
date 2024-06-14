package prefetcher

import chisel3._

// 存储每次内存访问信息的类
class AccessHistoryEntry extends Bundle {
  val address = UInt(5.W)       // 访问的地址
  val valid = Bool()            // 条目是否有效
  val prefetch = Bool()         // 是否为预取操作
  val timestamp = UInt(32.W)    // 记录访问的时间戳
}

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