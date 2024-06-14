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