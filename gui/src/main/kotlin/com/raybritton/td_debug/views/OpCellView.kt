package com.raybritton.td_debug.views

import com.raybritton.td_debug.format
import com.raybritton.td_debug.windows.MainWindow
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane

class OpCellView {
    @FXML
    lateinit var opCellBck: AnchorPane

    @FXML
    private lateinit var opLineNum: Label

    @FXML
    private lateinit var opByte: Label

    @FXML
    private lateinit var opBytes: Label

    @FXML
    private lateinit var opLine: Label

    @FXML
    private lateinit var current: ImageView

    @FXML
    private lateinit var breakpoint: ImageView

    fun update(data: MainWindow.OpData, showOriginalLine: Boolean) {
        opLineNum.text = data.op.lineNumber.toString()
        opByte.text = data.op.byteAddr.format(true)
        opLine.text = if (showOriginalLine) data.op.originalLine.trim() else data.op.processedLine
        opBytes.text = data.op.bytes.joinToString(" ") { it.format(true) }
        current.isVisible = data.isCurrentOp
        breakpoint.isVisible = data.breakpoint
    }
}