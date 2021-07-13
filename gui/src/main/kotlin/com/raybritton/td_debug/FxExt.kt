package com.raybritton.td_debug

import com.raybritton.td_debug.prefs.FilePrefs
import com.raybritton.td_debug.prefs.WindowPrefs
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import kotlin.reflect.KClass

fun Node.showFatalAlert(message: String) {
    val alert = Alert(Alert.AlertType.ERROR, message, ButtonType.OK)
    alert.setOnCloseRequest { closeWindow() }
    alert.showAndWait()
}

fun showAlert(message: String) {
    val alert = Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)
    alert.showAndWait()
}

fun Node.closeWindow() {
    (scene.window as Stage).close()
}

fun Node.openWindow(
    title: String,
    clazz: KClass<*>,
    file: String,
    width: Double,
    height: Double,
    dialog: Boolean,
    controller: Any?
) {
    val stage = Stage()
    val root = if (controller == null) {
        FXMLLoader.load<Parent>(clazz.java.classLoader.getResource(file))
    } else {
        val loader = FXMLLoader(clazz.java.classLoader.getResource(file))
        loader.setController(controller)
        loader.load()
    }
    stage.scene = Scene(root, width, height)
    WindowPrefs.setup(stage.scene, clazz.qualifiedName!!, scene.window)
    stage.title = title
    if (dialog) {
        stage.isResizable = false
        stage.isAlwaysOnTop = true
        stage.showAndWait()
    } else {
        stage.show()
    }
}

fun setupFilePicker(
    button: Button,
    textField: TextField,
    name: String,
    code: String,
    fileType: String?,
    onDone: ((String?) -> Unit)? = null
) {
    button.onAction = EventHandler {
        val chooser = FileChooser()
        var initDir = FilePrefs.load(code) ?: File(System.getProperty("user.home"))
        if (!textField.text.isNullOrEmpty()) {
            val file = File(textField.text)
            if (file.exists()) {
                initDir = file.parentFile
            }
        }
        if (fileType != null) {
            chooser.extensionFilters.add(FileChooser.ExtensionFilter(fileType, "*.$fileType"))
        }
        if (initDir.isFile) {
            initDir = initDir.parentFile
        }
        chooser.initialDirectory = initDir
        chooser.title = "Select $name"
        val selected = chooser.showOpenDialog(textField.scene.window)
        if (selected != null) {
            textField.text = selected.absolutePath
            FilePrefs.store(code, selected)
        }
        onDone?.invoke(selected?.absolutePath)
    }

    textField.onDragDropped = EventHandler { event ->
        if (event.dragboard.hasFiles()) {
            textField.text = event.dragboard.files[0].absolutePath
            event.isDropCompleted = true
        } else if (event.dragboard.hasString()) {
            textField.text = event.dragboard.string
            event.isDropCompleted = true
        } else {
            event.isDropCompleted = false
        }
        event.consume()
    }
}