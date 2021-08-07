package com.raybritton.td_debug.system

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TapeDebug(
    val ops: List<TapeOp>,
    val strings: List<TapeString>,
    val data: List<TapeData>
)

@Serializable
data class TapeOp(
    @SerialName("byte_addr")
    val byteAddr: UShort,
    @SerialName("original_line")
    val originalLine: String,
    @SerialName("line_num")
    val lineNumber: Int,
    val bytes: List<UByte>,
    @SerialName("processed_line")
    val processedLine: String,
)

@Serializable
data class TapeString(
    val addr: UShort,
    val key: String,
    val content: String,
    @SerialName("original_line")
    val originalLine: String,
    @SerialName("line_num")
    val lineNumber: Int,
    val usage: List<Usage>
)

@Serializable
data class TapeData(
    val addr: UShort,
    val key: String,
    val content: List<List<UByte>>,
    @SerialName("original_line")
    val originalLine: String,
    @SerialName("line_num")
    val lineNumber: Int,
    val usage: List<Usage>
)

@Serializable
data class Usage(
    @SerialName("op_addr")
    val opAddr: UShort,
    val offset: Int,
    val line: Int
)