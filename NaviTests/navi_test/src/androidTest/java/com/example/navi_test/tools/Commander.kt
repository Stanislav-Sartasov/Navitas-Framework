package com.example.navi_test.tools

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class Commander {
    fun execute(command: String) : String? {
        // Run the command
        val process = Runtime.getRuntime().exec(command)

        val bufferedReader = BufferedReader(
            InputStreamReader(process.inputStream)
        )
        // Grab the results
        val log = StringBuilder()
        var line: String?
        line = bufferedReader.readLine()
        while (line != null) {
            log.append(line + "\n")
            line = bufferedReader.readLine()
        }

        val reader = BufferedReader(
            InputStreamReader(process.errorStream)
        )
        // If we had an error during execution we get it here
        val errorLog = StringBuilder()
        var errorLine: String?
        errorLine = reader.readLine()
        while (errorLine != null) {
            errorLog.append(errorLine + "\n")
            errorLine = reader.readLine()
        }

        return when {
            errorLog.toString() != "" -> {
                Log.e("NaviProf", "command: $command log: $log error: $errorLine")
                null
            }
            log.toString() != "" -> {
                Log.i("NaviProf", "command: $command log: $log")
                log.toString()
            }
            else -> {
                null
            }
        }
    }
}