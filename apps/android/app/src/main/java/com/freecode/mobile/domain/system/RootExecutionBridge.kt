package com.freecode.mobile.domain.system

data class ShellExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

interface RootExecutionBridge {
    suspend fun execute(command: String, useRoot: Boolean): ShellExecutionResult
}
