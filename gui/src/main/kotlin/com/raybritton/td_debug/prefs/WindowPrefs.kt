package com.raybritton.td_debug.prefs

import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Window
import java.util.prefs.Preferences

object WindowPrefs {
    private const val KEY_SET = "window_prefs_set"
    private const val KEY_X = "window.x"
    private const val KEY_Y = "window.y"
    private const val KEY_WIDTH = "window.w"
    private const val KEY_HEIGHT = "window.h"
    private const val KEY_SCREEN = "window.s"

    fun setup(scene: Scene, code: String, from: Window?) {
        val prefs = Preferences.userRoot().node("${code}.window_pref")
        if (prefs.getBoolean(KEY_SET, false) && screensMatch(prefs, from)) {
            scene.window.load(prefs)
            if (!scene.window.isVisible()) {
                scene.window.setDefaultPosition()
            }
        } else if (from != null) {
            scene.window.setPosition(from)
        }
        scene.window.setOnCloseRequest {
            scene.window.save(prefs)
        }
    }

    private fun screensMatch(prefs: Preferences, other: Window?): Boolean {
        if (other == null) return true
        val savedScreen = prefs.getInt(KEY_SCREEN, 0)
        val newScreen = other.whichScreen()
        return savedScreen == newScreen
    }

    private fun Window.setPosition(parent: Window) {
        this.x = parent.x + 50
        this.y = parent.y + 50
    }

    private fun Window.load(prefs: Preferences) {
        this.x = prefs.getDouble(KEY_X, 0.0)
        this.y = prefs.getDouble(KEY_Y, 0.0)
        this.width = prefs.getDouble(KEY_WIDTH, 1000.0)
        this.height = prefs.getDouble(KEY_HEIGHT, 1000.0)
    }

    private fun Window.save(prefs: Preferences) {
        prefs.putDouble(KEY_X, this.x)
        prefs.putDouble(KEY_Y, this.y)
        prefs.putDouble(KEY_WIDTH, this.width)
        prefs.putDouble(KEY_HEIGHT, this.height)
        prefs.putInt(KEY_SCREEN, this.whichScreen())
        prefs.putBoolean(KEY_SET, true)
    }

    private fun Window.isVisible(): Boolean {
        return Screen.getScreens().any { it.bounds.contains(this.x, this.y, this.width, this.height) }
    }

    private fun Window.setDefaultPosition() {
        sizeToScene()
        val primary = Screen.getPrimary().bounds
        this.x = primary.minX + 50
        this.y = primary.minY + 50
    }

    private fun Window.whichScreen(): Int {
        return Screen.getScreensForRectangle(this.x, this.y, this.width, this.height)[0].bounds.hashCode()
    }
}