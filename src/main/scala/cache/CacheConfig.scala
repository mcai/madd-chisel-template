package cache

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

trait CacheConfig {
    val addressWidth = 32
    val dataWidth = 32

    val blockSizeInBytes = 16
    val assoc = 1
    val numSets = 128

    val capacityInSize = blockSizeInBytes * assoc * numSets // 16 * assoc * 128 bytes = 2^4 * assoc * 2^7 bytes = 2^11 * assoc bytes = 2 * assoc kB

    val offsetBits = log2Ceil(blockSizeInBytes) // log2(16) = 4
    val indexBits = log2Ceil(numSets) // log2(128) = 7
    val tagBits = addressWidth - indexBits - offsetBits // 21
}