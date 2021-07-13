package com.raybritton.td_debug.views

import com.raybritton.td_debug.format
import com.raybritton.td_debug.system.mnemonic
import com.raybritton.td_debug.windows.JmpDest
import com.raybritton.td_debug.windows.OpExe
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane

class OpExeCellView {
    @FXML
    lateinit var opCellBck: AnchorPane

    @FXML
    private lateinit var opLineNum: Label

    @FXML
    private lateinit var opByte: Label

    @FXML
    private lateinit var opMnemonic: Label

    @FXML
    private lateinit var destLineNum: Label

    @FXML
    private lateinit var destByte: Label

    @FXML
    private lateinit var arrow: Label

    fun update(data: OpExe) {
        opLineNum.text = data.op.lineNumber.toString()
        opByte.text = data.op.byteAddr.format(true)
        opMnemonic.text = data.op.bytes[0].mnemonic()
        when (data.type) {
            is JmpDest.BadTarget -> {
                destByte.text = data.type.addr.format(true)
                destLineNum.text = "??"
                arrow.isVisible = true
            }
            is JmpDest.Jump -> {
                destByte.text = data.type.dest.byteAddr.format(true)
                destLineNum.text = data.type.dest.lineNumber.toString()
                arrow.isVisible = true
            }
            JmpDest.NoJump -> {
                destByte.text = ""
                destLineNum.text = ""
                arrow.isVisible = false
            }
        }
    }
}