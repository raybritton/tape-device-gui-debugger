package com.raybritton.td_debug.system

import com.raybritton.td_debug.Request
import com.raybritton.td_debug.Response
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import kotlin.concurrent.thread

typealias OnBreakpointHit = (UShort) -> Unit
typealias OnMemoryResponse = (UShort, UShort, List<UByte>) -> Unit
typealias OnOutput = (String) -> Unit
typealias OnError = (String) -> Unit
typealias OnDump = (TapeDevice.DeviceRepresentation) -> Unit
typealias OnProcessFinish = () -> Unit
typealias OnStep = () -> Unit
typealias OnCharRequest = () -> Unit
typealias OnStringRequest = () -> Unit

object TapeDevice {
    private lateinit var process: Process
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var error: InputStream
    var state = State.READY
        private set
    var deviceState = DeviceRepresentation(0u, 0u, 0u, 0u, listOf(0u, 0u, 0u, 0u), listOf(0u, 0u), false)

    val onBreakpointHits = mutableListOf<OnBreakpointHit>()
    val onMemoryResponses = mutableListOf<OnMemoryResponse>()
    val onOutputs = mutableListOf<OnOutput>()
    val onErrors = mutableListOf<OnError>()
    val onDumps = mutableListOf<OnDump>()
    val onProcessFinish = mutableListOf<OnProcessFinish>()
    val onSteps = mutableListOf<OnStep>()
    val onCharRequests = mutableListOf<OnCharRequest>()
    val onStringRequests = mutableListOf<OnStringRequest>()

    fun start(exePath: String, tapePath: String, inputPaths: List<String>) {
        process = ProcessBuilder(exePath, tapePath, "--piped")
            .start()

        input = process.inputStream
        output = process.outputStream
        error = process.errorStream

        thread {
            while (state == State.READY) {
                when (val cmd = readByte()) {
                    Response.Output.cmd -> {
                        val len = readByte()
                        val textBytes = ByteArray(len.toInt())
                        input.read(textBytes)
                        val text = String(textBytes)
                        onOutputs.forEach { it(text) }
                    }
                    Response.Error.cmd -> {
                        val len = readByte()
                        val textBytes = ByteArray(len.toInt())
                        input.read(textBytes)
                        val text = String(textBytes)
                        onErrors.forEach { it(text) }
                    }
                    Response.Dump.cmd -> {
                        deviceState = DeviceRepresentation.buildFromStream(input)
                        onDumps.forEach { it(deviceState) }
                    }
                    Response.BreakpointHit.cmd -> {
                        val addr = input.readNBytes(2).toUShort()
                        onBreakpointHits.forEach { it(addr) }
                    }
                    Response.Memory.cmd -> {
                        val start = input.readAddr()
                        val end = input.readAddr()
                        val len = input.readAddr().toInt()
                        println("Received mem resp from $start to $end ($len bytes)")
                        val mem = input.readNBytes(len).map { it.toUByte() }
                        onMemoryResponses.forEach { it(start, end, mem) }
                    }
                    Response.RequestChar.cmd -> {
                        onCharRequests.forEach { it() }
                    }
                    Response.RequestString.cmd -> {
                        onStringRequests.forEach { it() }
                    }
                    Response.Halted.cmd -> {
                        state = State.FINISHED
                    }
                    Response.Crashed.cmd -> {
                        state = State.FINISHED
                    }
                    else -> {
                        println("Received unexpected $cmd")
                    }
                }

                sleep(10)
            }
        }

        thread {
            while (state == State.READY) {
                val text = error.bufferedReader().readLine()
                if (text == null) {
                    println("Process has disconnected")
                    state = State.FINISHED
                    return@thread
                }
                onErrors.forEach { it(text) }
                sleep(10)
            }
        }

        thread {
            while (state == State.READY) {
                if (!process.isAlive) {
                    state = State.FINISHED
                    onProcessFinish.forEach { it() }
                }
                sleep(10)
            }
        }
    }

    private fun readByte(): UByte {
        val byte = byteArrayOf(0)
        input.read(byte)
        return byte[0].toUByte()
    }

    enum class State {
        READY,
        FINISHED
    }

    fun disconnect() {
        process.destroy()
    }

    fun step() {
        output.write(byteArrayOf(Request.Step.cmd.toByte(), Request.Dump.cmd.toByte()))
        output.flush()
        onSteps.forEach { it() }
    }

    fun requestDump() {
        output.write(byteArrayOf(Request.Dump.cmd.toByte()))
        output.flush()
    }

    fun stepForce() {
        output.write(byteArrayOf(Request.StepIgnoreBreakpoint.cmd.toByte(), Request.Dump.cmd.toByte()))
        output.flush()
        onSteps.forEach { it() }
    }

    fun setBreakpoint(addr: UShort) {
        val bytes = addr.toBytes()
        output.write(byteArrayOf(Request.SetBreakpoint.cmd.toByte(), bytes[1], bytes[0]))
        output.flush()
    }

    fun clearBreakpoint(addr: UShort) {
        val bytes = addr.toBytes()
        output.write(byteArrayOf(Request.ClearBreakpoint.cmd.toByte(), bytes[1], bytes[0]))
        output.flush()
    }

    fun requestMemory(start: UShort, end: UShort) {
        println("Requesting mem from $start to $end")
        val startBytes = start.toBytes()
        val endBytes = end.toBytes()
        output.write(byteArrayOf(Request.RequestMemory.cmd.toByte(), startBytes[1], startBytes[0], endBytes[1], endBytes[0]))
        output.flush()
    }

    fun sendChar(chr: UByte) {
        output.write(byteArrayOf(Request.InputChar.cmd.toByte(), chr.toByte()))
        output.flush()
    }

    fun sendString(str: String) {
        output.write(byteArrayOf(Request.InputString.cmd.toByte(), str.length.toByte()))
        output.write(str.toByteArray())
        output.flush()
    }

    private fun UShort.toBytes(): ByteArray {
        return byteArrayOf((this.toInt() and 0x00FF).toByte(), ((this.toInt() and 0xFF00) shr (8)).toByte())
    }

    fun ByteArray.toUShort(): UShort {
        return (((this[0].toInt() and 255) shl 8) or (this[1].toInt() and 255)).toUShort()
    }

    private fun InputStream.readAddr(): UShort {
        return this.readNBytes(2).toUShort()
    }

    data class DeviceRepresentation(
        val pc: UShort,
        val sp: UShort,
        val acc: UByte,
        val fp: UShort,
        val dataReg: List<UByte>,
        val addrReg: List<UShort>,
        val overflow: Boolean
    ) {
        companion object {
            fun buildFromStream(inputStream: InputStream): DeviceRepresentation {
                return DeviceRepresentation(
                    pc = inputStream.readAddr(),
                    addrReg = listOf(inputStream.readAddr(),inputStream.readAddr()),
                    sp = inputStream.readAddr(),
                    fp = inputStream.readAddr(),
                    acc = readByte(),
                    dataReg = listOf(readByte(),readByte(),readByte(),readByte()),
                    overflow = readByte().toInt() == 1
                )
            }
        }
    }
}

