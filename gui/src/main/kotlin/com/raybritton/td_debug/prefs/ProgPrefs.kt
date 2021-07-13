package com.raybritton.td_debug.prefs

import java.util.prefs.Preferences

object ProgPrefs {
    private const val KEY_EXE_PATH = "debugger.exe_path"

    private val prefs = Preferences.userRoot().node("TapeDeviceDebugger")

    var exePath: String? = prefs.get(KEY_EXE_PATH, null)
        set(value) {
            field = value
            prefs.put(KEY_EXE_PATH, value)
        }
}