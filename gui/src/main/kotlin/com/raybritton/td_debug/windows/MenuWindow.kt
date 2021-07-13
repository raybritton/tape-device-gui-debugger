package com.raybritton.td_debug.windows

import com.raybritton.td_debug.closeWindow
import com.raybritton.td_debug.prefs.FilePrefs
import com.raybritton.td_debug.prefs.ProgPrefs
import com.raybritton.td_debug.setupFilePicker
import com.raybritton.td_debug.showFatalAlert
import com.raybritton.td_debug.system.ExeChecker
import com.raybritton.td_debug.system.TapeDebug
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.concurrent.thread

class MenuWindow {
    @FXML
    private lateinit var settings: Button

    @FXML
    private lateinit var startDebug: Button

    @FXML
    private lateinit var debugChoose: Button

    @FXML
    private lateinit var tapeChoose: Button

    @FXML
    private lateinit var tapeFileField: TextField

    @FXML
    private lateinit var debugFileField: TextField

    @FXML
    private lateinit var loading: VBox

    @FXML
    private fun initialize() {
        startDebug.isDisable = true

        if (ProgPrefs.exePath == null || ExeChecker.checkExeIsCompat(ProgPrefs.exePath!!) != ExeChecker.Compat.Ok) {
            Platform.runLater {
                SettingsWindow.open(settings)
            }
        }

        tapeFileField.text = FilePrefs.load("tapefile")?.absolutePath ?: ""
        debugFileField.text = FilePrefs.load("debugfile")?.absolutePath ?: ""

        validateFiles()

        setupFilePicker(tapeChoose, tapeFileField, "Tape file", "tapefile", "tape") { validateFiles() }
        setupFilePicker(debugChoose, debugFileField, "Debug file", "debugfile", "debug") { validateFiles() }

        settings.onAction = EventHandler {
            SettingsWindow.open(settings)
        }

        startDebug.onAction = EventHandler {
            loading.isVisible = true
            thread {
                val debugText = File(debugFileField.text).readText()
                val json = Json {
                    ignoreUnknownKeys = true
                }
                try {
                    val debug: TapeDebug = json.decodeFromString(debugText)
                    Platform.runLater {
                        MainWindow.open(settings, tapeFileField.text, debug)
                        settings.closeWindow()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Platform.runLater {
                        settings.showFatalAlert("Invalid JSON\n" + e.message)
                    }
                }
            }
        }
    }

    private fun validateFiles() {
        startDebug.isDisable = true
        if (debugFileField.text.isNotEmpty()) {
            val debugFile = File(debugFileField.text)
            if (!debugFile.canRead() || !debugFile.isFile) {
                return
            }
        } else {
            return
        }
        if (tapeFileField.text.isNotEmpty()) {
            val tapeFile = File(tapeFileField.text)
            if (!tapeFile.canRead() || !tapeFile.isFile) {
                return
            }
        }
        else {
            return
        }
        startDebug.isDisable = false
    }
}