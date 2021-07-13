package com.raybritton.td_debug.windows

import com.raybritton.td_debug.format
import com.raybritton.td_debug.openWindow
import com.raybritton.td_debug.system.TapeString
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.MapValueFactory

class StringsWindow(private val strings: List<TapeString>) {
    @FXML
    private lateinit var table: TableView<Map<String, String>>

    @FXML
    private fun initialize() {
        table.columns.add(createColumn("Address", "address"))
        table.columns.add(createColumn("Key", "key"))
        table.columns.add(createColumn("Content", "content"))

        table.items.addAll(strings.map {
            mapOf(
                "address" to it.addr.format(true),
                "key" to it.key,
                "content" to it.content
            )
        })
    }

    private fun createColumn(name: String, code: String): TableColumn<Map<String, String>, String> {
        val column = TableColumn<Map<*,*>, String>(name)
        column.cellValueFactory = MapValueFactory(code)
        return column as TableColumn<Map<String, String>, String>
    }

    companion object {
        fun open(node: Node, strings: List<TapeString>) {
            val controller = StringsWindow(strings)
            node.openWindow("String", StringsWindow::class, "strings.fxml", 800.0, 800.0, false, controller)
        }
    }
}