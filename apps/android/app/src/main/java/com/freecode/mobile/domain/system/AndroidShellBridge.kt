package com.freecode.mobile.domain.system

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidShellBridge : RootExecutionBridge {
    override suspend fun execute(command: String, useRoot: Boolean): ShellExecutionResult =
        withContext(Dispatchers.IO) {
            val process = if (useRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ShellExecutionResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
        }
}
