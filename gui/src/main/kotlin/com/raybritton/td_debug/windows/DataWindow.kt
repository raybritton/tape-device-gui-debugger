package com.raybritton.td_debug.windows

import com.raybritton.td_debug.format
import com.raybritton.td_debug.isLetter
import com.raybritton.td_debug.openWindow
import com.raybritton.td_debug.system.TapeData
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView

class DataWindow(private val data: List<Pair<String,List<String>>>) {
    @FXML
    private lateinit var tree: TreeView<String>

    @FXML
    private fun initialize() {
        val root = TreeItem("Data")
        root.isExpanded = true
        for (datum in data) {
            val def = TreeItem(datum.first)
            for (array in datum.second) {
                def.children.add(TreeItem(array))
            }
            root.children.add(def)
        }
        tree.root = root
    }

    companion object {
        fun open(node: Node, debug: List<TapeData>) {
            val data = debug.map {
                val key = it.key + " " + it.addr.format(true)
                Pair(key, it.content.map {
                    val hex = it.joinToString("") {
                        it.format(true)
                    }
                    if (it.any { it.isLetter() }) {
                        return@map hex + " " + String(it.map { it.toByte() }.toByteArray())
                    } else {
                        return@map hex
                    }
                })
            }
            val controller = DataWindow(data)
            node.openWindow("Data", DataWindow::class, "data.fxml", 800.0, 800.0, false, controller)
        }
    }
}