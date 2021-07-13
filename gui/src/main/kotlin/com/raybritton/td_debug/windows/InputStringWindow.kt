package com.raybritton.td_debug.windows

import com.raybritton.td_debug.closeWindow
import com.raybritton.td_debug.openWindow
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField

typealias OnStrClose = (String) -> Unit

class InputStringWindow(private val onClose: OnStrClose) {

    @FXML
    private lateinit var submit: Button

    @FXML
    private lateinit var input: TextField

    @FXML
    private fun initialize() {
        submit.onAction = EventHandler {
            onClose(input.text)
            submit.closeWindow()
        }
    }

    companion object {
        fun open(node: Node, onClose: OnStrClose) {
            val controller = InputStringWindow(onClose)
            node.openWindow("Input String", InputStringWindow::class, "input_str.fxml", 490.0, 140.0, true, controller)
        }
    }
}