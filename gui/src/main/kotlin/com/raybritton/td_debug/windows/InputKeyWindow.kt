package com.raybritton.td_debug.windows

import com.raybritton.td_debug.closeWindow
import com.raybritton.td_debug.openWindow
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode

typealias OnClose = (UByte) -> Unit

class InputKeyWindow(private val onClose: OnClose) {

    @FXML
    private lateinit var lbl: TextArea

    @FXML
    private fun initialize() {
        Platform.runLater {
            lbl.scene.onKeyReleased = EventHandler { key ->
                if (key.text.isEmpty()) {
                    when (key.code) {
                        KeyCode.ESCAPE -> onClose(27u)
                        KeyCode.ENTER -> onClose(10u)
                        KeyCode.BACK_SPACE -> onClose(8u)
                        KeyCode.TAB -> onClose(9u)
                        KeyCode.DELETE -> onClose(127u)
                    }
                } else {
                    onClose(key.text[0].code.toUByte())
                }
                lbl.closeWindow()
            }
        }
    }

    companion object {
        fun open(node: Node, onClose: OnClose) {
            val controller = InputKeyWindow(onClose)
            node.openWindow("Input Key", InputKeyWindow::class, "input_key.fxml", 170.0, 180.0, true, controller)
        }
    }
}