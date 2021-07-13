package com.raybritton.td_debug.prefs

import java.io.File
import java.util.prefs.Preferences

object FilePrefs {
    private val prefs = Preferences.userRoot().node("td_debugger_file_prefs")

    fun store(code: String, path: File) {
        prefs.put(code, path.absolutePath)
    }

    fun load(code: String): File? {
        val path = prefs.get(code, null)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                return file
            } else if (file.parentFile.exists()) {
                return file.parentFile
            }
        }
        return null
    }
}