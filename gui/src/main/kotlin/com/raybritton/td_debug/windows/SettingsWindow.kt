package com.raybritton.td_debug.windows

import com.raybritton.td_debug.MAIN_TD_VERSION
import com.raybritton.td_debug.closeWindow
import com.raybritton.td_debug.openWindow
import com.raybritton.td_debug.prefs.ProgPrefs
import com.raybritton.td_debug.setupFilePicker
import com.raybritton.td_debug.system.ExeChecker
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField

class SettingsWindow {
    @FXML
    private lateinit var exePathField: TextField

    @FXML
    private lateinit var saveAndClose: Button

    @FXML
    private lateinit var cancel: Button

    @FXML
    private lateinit var openExe: Button

    @FXML
    private lateinit var error: Label

    @FXML
    private lateinit var info: Label

    @FXML
    private fun initialize() {
        setupLabels(ProgPrefs.exePath)

        setupFilePicker(openExe, exePathField, "Tape Device", "tdexe_path", "tape") { path ->
            setupLabels(path)
        }

        exePathField.text = ProgPrefs.exePath

        saveAndClose.onAction = EventHandler {
            ProgPrefs.exePath = exePathField.text
            saveAndClose.closeWindow()
        }
        cancel.onAction = EventHandler {
            cancel.closeWindow()
        }
    }

    private fun setupLabels(path: String?) {
        if (path == null) {
            error.isVisible = false
            info.isVisible = true
            info.text = "Select a Tape Device exe"
            return
        }
        when (val result = ExeChecker.checkExeIsCompat(path)) {
            ExeChecker.Compat.Ok -> {
                error.isVisible = false
                info.isVisible = false
            }
            ExeChecker.Compat.WrongVersion -> {
                info.isVisible = false
                error.isVisible = true
                error.text = "Tape Device exe is wrong version, should be $MAIN_TD_VERSION"
            }
            ExeChecker.Compat.Other, ExeChecker.Compat.Directory -> {
                info.isVisible = false
                error.isVisible = true
                error.text = "Not a Tape Device exe"
            }
            is ExeChecker.Compat.Error -> {
                info.isVisible = false
                error.isVisible = true
                error.text = "Error: ${result.msg}"
            }
        }
    }

    companion object {
        fun open(node: Node) {
            node.openWindow("Settings", SettingsWindow::class, "settings.fxml", 400.0, 200.0, true, null)
        }
    }
}