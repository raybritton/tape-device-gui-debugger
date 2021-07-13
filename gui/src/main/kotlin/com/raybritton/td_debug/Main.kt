package com.raybritton.td_debug

import com.raybritton.td_debug.prefs.WindowPrefs
import javafx.application.Application
import javafx.application.Application.launch
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    launch(App::class.java)
}

class App : Application() {
    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("menu.fxml"))
        stage.scene = Scene(root, 500.0, 180.0)
        WindowPrefs.setup(stage.scene, "Menu", null)
        stage.title = "Tape Device Debugger"
        stage.isResizable = false
        stage.show()
    }
}