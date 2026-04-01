package com.freecode.mobile.ui.state

data class ShellUiState(
    val command: String = "id",
    val useRoot: Boolean = true,
    val running: Boolean = false,
    val exitCode: Int? = null,
    val stdout: String = "",
    val stderr: String = "",
)
