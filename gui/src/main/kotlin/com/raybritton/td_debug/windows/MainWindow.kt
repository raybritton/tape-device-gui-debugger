package com.raybritton.td_debug.windows

import com.raybritton.td_debug.*
import com.raybritton.td_debug.prefs.ProgPrefs
import com.raybritton.td_debug.system.TapeDebug
import com.raybritton.td_debug.system.TapeDevice
import com.raybritton.td_debug.system.TapeDevice.toUShort
import com.raybritton.td_debug.system.TapeOp
import com.raybritton.td_debug.system.isJumpOp
import com.raybritton.td_debug.views.OpCellView
import com.raybritton.td_debug.views.OpExeCellView
import javafx.application.Platform
import javafx.collections.FXCollections.observableArrayList
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.skin.ListViewSkin
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import kotlin.concurrent.thread

class MainWindow(private val tapePath: String, private val debug: TapeDebug) {
    @FXML
    private lateinit var tdOutput: TextArea

    @FXML
    private lateinit var tdStep: Button

    @FXML
    private lateinit var tdRun: Button

    @FXML
    private lateinit var tdDump: TextArea

    @FXML
    private lateinit var addrHex: RadioButton

    @FXML
    private lateinit var regHex: RadioButton

    @FXML
    private lateinit var tdOps: ListView<OpData>

    @FXML
    private lateinit var openStrings: Button

    @FXML
    private lateinit var openData: Button

    @FXML
    private lateinit var tdStatus: Label

    @FXML
    private lateinit var opsFollowPc: CheckBox

    @FXML
    private lateinit var inputStr: Button

    @FXML
    private lateinit var inputStrWarning: Label

    @FXML
    private lateinit var inputChar: Button

    @FXML
    private lateinit var inputCharWarning: Label

    @FXML
    private lateinit var autoRun: CheckBox

    @FXML
    private lateinit var history: ListView<OpExe>

    @FXML
    private lateinit var historyPane: VBox

    @FXML
    private lateinit var toggleMemory: Button

    @FXML
    private lateinit var toggleHistory: Button

    @FXML
    private lateinit var memoryPane: AnchorPane

    @FXML
    private lateinit var memoryStart: TextField

    @FXML
    private lateinit var memoryEnd: TextField

    @FXML
    private lateinit var memOutput: TextArea

    @FXML
    private lateinit var memoryLoading: ProgressIndicator

    @FXML
    private lateinit var memoryStack: CheckBox

    @FXML
    private lateinit var memChars: CheckBox

    @FXML
    private lateinit var memUpdate: Button

    @FXML
    private lateinit var memHex: RadioButton

    @FXML
    private lateinit var memDec: RadioButton

    @FXML
    private lateinit var memBin: RadioButton

    @FXML
    private lateinit var memAddrHex: RadioButton

    @FXML
    private lateinit var memAddrDec: RadioButton

    private val ops = observableArrayList(debug.ops.map { OpData(it, false, false) })

    private var runner = setupRunnerThread()
    private var running = false
    private var killThread = false
    private var startAddr: UShort = 0u
    private var endAddr: UShort = 255u

    private var lastMemStart: UShort = 0u
    private var lastMemEnd: UShort = 0u
    private var lastMemBytes = emptyList<UByte>()

    @FXML
    private fun initialize() {
        setupUi()
        setupUiListeners()
        setupDeviceListeners()

        TapeDevice.start(ProgPrefs.exePath!!, tapePath, listOf())
        TapeDevice.requestDump()
    }

    private fun requestMemory() {
        if (TapeDevice.state == TapeDevice.State.READY) {
            if (memoryStack.isSelected) {
                memoryLoading.isVisible = true
                TapeDevice.requestMemory(TapeDevice.deviceState.sp, UShort.MAX_VALUE)
            } else {
                val newStart = memoryStart.text.trim().toUShortOrNull(16)
                val newEnd = memoryEnd.text.trim().toUShortOrNull(16)
                if (newStart == null || newEnd == null) {
                    return
                }
                memoryLoading.isVisible = true
                TapeDevice.requestMemory(newStart, newEnd)
            }
        }
    }

    private fun updateState() {
        populateDump(TapeDevice.deviceState)
        val pc = TapeDevice.deviceState.pc
        for (op in ops) {
            op.isCurrentOp = op.op.byteAddr == pc
        }
        tdOps.refresh()
        if (opsFollowPc.isSelected) {
            try {
                val idx = ops.indexOfFirst { it.isCurrentOp }
                val flow = ((tdOps.skin as? ListViewSkin<*>)?.children?.get(0) as? VirtualFlow<*>)
                if (flow != null && (idx < flow.firstVisibleCell.index || idx > flow.lastVisibleCell.index)) {
                    Platform.runLater { tdOps.scrollTo(idx) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun populateDump(state: TapeDevice.DeviceRepresentation) {
        val hex16Bit = addrHex.isSelected
        val hex8Bit = regHex.isSelected
        val output = StringBuilder()
        output.append("PC:" + state.pc.format(hex16Bit))
        output.append("  ACC:" + state.acc.formatWithChar(hex8Bit))
        output.append("\nA0:" + state.addrReg[0].format(hex16Bit))
        output.append("  A1:" + state.addrReg[1].format(hex16Bit))
        output.append("\nSP:" + state.sp.format(hex16Bit))
        output.append("  FP:" + state.fp.format(hex16Bit))
        output.append("  Over: " + state.overflow)
        output.append("\nD0:" + state.dataReg[0].formatWithChar(hex8Bit))
        output.append("  D1:" + state.dataReg[1].formatWithChar(hex8Bit))
        output.append("  D2:" + state.dataReg[2].formatWithChar(hex8Bit))
        output.append("  D3:" + state.dataReg[3].formatWithChar(hex8Bit))
        synchronized(tdDump) {
            tdDump.text = output.toString()
        }
    }

    private fun addHistory() {
        if (historyPane.isManaged) {
            val data = tdOps.items.first { it.isCurrentOp }
            val item = if (data.op.bytes[0].isJumpOp()) {
                val destByte = byteArrayOf(data.op.bytes[1].toByte(), data.op.bytes[2].toByte()).toUShort()
                val dest = tdOps.items.firstOrNull { it.op.byteAddr == destByte }
                if (dest == null) {
                    OpExe(data.op, JmpDest.BadTarget(destByte))
                } else {
                    OpExe(data.op, JmpDest.Jump(dest.op))
                }
            } else {
                OpExe(data.op, JmpDest.NoJump)
            }
            Platform.runLater {
                history.items.add(item)
            }
        }
    }

    private fun setupRunnerThread(): Thread {
        return thread {
            while (!killThread) {
                while (running) {
                    addHistory()
                    TapeDevice.step()
                    Thread.sleep(30)
                }
                Thread.sleep(100)
            }
        }
    }

    private fun setupUi() {
        memoryStart.text = startAddr.format(true)
        memoryEnd.text = endAddr.format(true)
        memoryPane.isVisible = false
        memoryPane.isManaged = false
        historyPane.isVisible = false
        historyPane.isManaged = false

        tdOps.cellFactory = Callback { OpCell() }
        tdOps.items = ops
        history.cellFactory = Callback { OpExeCell() }
    }

    private fun setupUiListeners() {
        toggleMemory.onAction = EventHandler {
            memoryPane.isVisible = !memoryPane.isVisible
            memoryPane.isManaged = !memoryPane.isManaged
            if (memoryPane.isManaged) {
                requestMemory()
            }
        }

        toggleHistory.onAction = EventHandler {
            historyPane.isVisible = !historyPane.isVisible
            historyPane.isManaged = !historyPane.isManaged
            if (!historyPane.isManaged) {
                history.items.clear()
            }
        }

        tdStep.onAction = EventHandler {
            if (inputCharWarning.isVisible || inputCharWarning.isVisible) {
                showAlert("Device is waiting for input")
                return@EventHandler
            }
            addHistory()
            TapeDevice.stepForce()
        }

        tdRun.onAction = EventHandler {
            if (inputCharWarning.isVisible || inputCharWarning.isVisible) {
                showAlert("Device is waiting for input")
                return@EventHandler
            }
            running = !running
            autoRun.isSelected = running
        }

        inputChar.onAction = EventHandler {
            InputKeyWindow.open(inputChar) { chr ->
                TapeDevice.sendChar(chr)
                TapeDevice.requestDump()
                inputCharWarning.isVisible = false
                if (autoRun.isSelected) {
                    running = true
                }
            }
        }

        inputStr.onAction = EventHandler {
            InputStringWindow.open(inputStr) { str ->
                TapeDevice.sendString(str)
                TapeDevice.requestDump()
                inputStr.isDisable = true
                inputStrWarning.isVisible = false
                if (autoRun.isSelected) {
                    running = true
                }
            }
        }

        addrHex.selectedProperty().addListener { _, _, _ -> updateState() }
        regHex.selectedProperty().addListener { _, _, _ -> updateState() }

        openStrings.onAction = EventHandler {
            StringsWindow.open(openStrings, debug.strings)
        }

        openData.onAction = EventHandler {
            DataWindow.open(openData, debug.data)
        }

        memoryStack.selectedProperty().addListener { _, _, newValue ->
            memoryStart.isDisable = newValue
            memoryEnd.isDisable = newValue
        }

        val redrawMemory = EventHandler<Event> {
            redrawMem()
        }

        memAddrHex.onAction = redrawMemory as EventHandler<ActionEvent>
        memHex.onAction = redrawMemory
        memDec.onAction = redrawMemory
        memChars.onAction = redrawMemory
        memAddrDec.onAction = redrawMemory
        memBin.onAction = redrawMemory
        memUpdate.onAction = EventHandler { requestMemory() }

        tdOps.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                val isBreakpoint = tdOps.selectionModel.selectedItem.breakpoint
                val addr = tdOps.selectionModel.selectedItem.op.byteAddr
                if (isBreakpoint) {
                    TapeDevice.clearBreakpoint(addr)
                } else {
                    TapeDevice.setBreakpoint(addr)
                }
                tdOps.selectionModel.selectedItem.breakpoint = !tdOps.selectionModel.selectedItem.breakpoint
                val temp = opsFollowPc.isSelected
                opsFollowPc.isSelected = false
                updateState()
                opsFollowPc.isSelected = temp
            }
        }
    }

    private fun setupDeviceListeners() {
        val breakpointHit = { addr: UShort ->
            running = false
        }
        val textOutput = { text: String -> tdOutput.text += text }
        val errorOutput = { text: String -> tdOutput.text += text }
        val processFinished = {
            running = false
            Platform.runLater {
                tdStatus.text = "Tape Device stopped (HALT or EoF)"
                tdStep.isDisable = true
                tdRun.isDisable = true
                memUpdate.isDisable = true
                inputStr.isDisable = true
                inputChar.isDisable = true
            }
        }
        val onCharRequest = {
            running = false
            Platform.runLater {
                inputCharWarning.isVisible = true
            }
        }
        val onStringRequest = {
            running = false
            Platform.runLater {
                inputStrWarning.isVisible = true
                inputStr.isDisable = false
            }
        }

        val onStep = {
            if (memoryPane.isManaged) {
                requestMemory()
            }
        }

        val memListener = { startMemAddr: UShort, endMemAddr: UShort, bytes: List<UByte> ->
            lastMemStart = startMemAddr
            lastMemEnd = endMemAddr
            lastMemBytes = bytes
            redrawMem()
        }

        val onDump = { _: TapeDevice.DeviceRepresentation ->
            updateState()
        }

        TapeDevice.onOutputs.add(textOutput)
        TapeDevice.onErrors.add(errorOutput)
        TapeDevice.onBreakpointHits.add(breakpointHit)
        TapeDevice.onProcessFinish.add(processFinished)
        TapeDevice.onCharRequests.add(onCharRequest)
        TapeDevice.onStringRequests.add(onStringRequest)
        TapeDevice.onSteps.add(onStep)
        TapeDevice.onMemoryResponses.add(memListener)
        TapeDevice.onDumps.add(onDump)

        Platform.runLater {
            tdOps.scene.window.onHiding = EventHandler {
                TapeDevice.onOutputs.remove(textOutput)
                TapeDevice.onErrors.remove(errorOutput)
                TapeDevice.onBreakpointHits.remove(breakpointHit)
                TapeDevice.onProcessFinish.remove(processFinished)
                TapeDevice.onCharRequests.remove(onCharRequest)
                TapeDevice.onStringRequests.remove(onStringRequest)
                TapeDevice.onSteps.remove(onStep)
                TapeDevice.onMemoryResponses.remove(memListener)
                TapeDevice.onDumps.remove(onDump)
                TapeDevice.disconnect()
                running = false
                killThread = true
                runner.join()
            }
        }
    }

    private fun redrawMem() {
        if (!memoryStack.isSelected) {
            memoryStart.text = lastMemStart.format(true)
            memoryEnd.text = lastMemEnd.format(true)
        }
        if (memAddrHex.isSelected) {
            MemoryFormatter.addrFormat = MemoryFormatter.AddrFormat.HEX
        } else {
            MemoryFormatter.addrFormat = MemoryFormatter.AddrFormat.DEC
        }
        if (memHex.isSelected) {
            MemoryFormatter.memFormat = MemoryFormatter.MemFormat.HEX
        } else if (memDec.isSelected) {
            MemoryFormatter.memFormat = MemoryFormatter.MemFormat.DEC
        } else {
            MemoryFormatter.memFormat = MemoryFormatter.MemFormat.BIN
        }
        MemoryFormatter.showLetters = memChars.isSelected
        val memText = MemoryFormatter.formatMemory(lastMemStart, lastMemBytes)
        Platform.runLater {
            memoryLoading.isVisible = false
            synchronized(memOutput) {
                memOutput.text = memText
            }
        }
    }

    companion object {
        fun open(node: Node, tapePath: String, debug: TapeDebug) {
            val controller = MainWindow(tapePath, debug)
            node.openWindow("Debugger", MainWindow::class, "main.fxml", 1600.0, 1000.0, false, controller)
        }
    }
}

data class OpExe(
    val op: TapeOp,
    val type: JmpDest
)

sealed class JmpDest {
    object NoJump : JmpDest()
    class BadTarget(val addr: UShort) : JmpDest()
    class Jump(val dest: TapeOp) : JmpDest()
}

class OpExeCell : ListCell<OpExe>() {
    private val view = OpExeCellView()

    init {
        val loader = FXMLLoader(javaClass.classLoader.getResource("op_exe_cell.fxml"))
        loader.setController(view)
        loader.load<Parent>()
    }

    override fun updateItem(item: OpExe?, empty: Boolean) {
        super.updateItem(item, empty)
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        graphic = if (empty || item == null) {
            null
        } else {
            view.update(item)
            view.opCellBck
        }
    }
}

data class OpData(
    val op: TapeOp,
    var breakpoint: Boolean,
    var isCurrentOp: Boolean
)

class OpCell : ListCell<OpData>() {
    private val view = OpCellView()

    init {
        val loader = FXMLLoader(javaClass.classLoader.getResource("op_cell.fxml"))
        loader.setController(view)
        loader.load<Parent>()
    }

    override fun updateItem(item: OpData?, empty: Boolean) {
        super.updateItem(item, empty)
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        graphic = if (empty || item == null) {
            null
        } else {
            view.update(item)
            view.opCellBck
        }
    }
}