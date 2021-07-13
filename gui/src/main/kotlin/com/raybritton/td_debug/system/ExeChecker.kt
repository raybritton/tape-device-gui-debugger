package com.raybritton.td_debug.system

import com.raybritton.td_debug.COMPAT_TD_VERSIONS
import java.io.File

object ExeChecker {
    fun checkExeIsCompat(exePath: String): Compat {
        if (File(exePath).isDirectory) {
            return Compat.Directory
        }

        try {
            val line = ProcessBuilder(exePath, "-h")
                .start()
                .inputStream
                .bufferedReader()
                .readLine()

            if (line.startsWith("tape_device")) {
                for (version in COMPAT_TD_VERSIONS) {
                    if (line.startsWith("tape_device $version")) {
                        return Compat.Ok
                    }
                }
                return Compat.WrongVersion
            } else {
                return Compat.Other;
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Compat.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    sealed class Compat {
        object Ok : Compat()
        object WrongVersion : Compat()
        object Directory : Compat()
        object Other : Compat()
        class Error(val msg: String) : Compat()
    }
}