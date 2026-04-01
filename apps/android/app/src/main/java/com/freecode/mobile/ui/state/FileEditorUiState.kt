package com.freecode.mobile.ui.state

data class FileEditorUiState(
    val activeWorkspacePath: String = "",
    val selectedFilePath: String = "",
    val selectedFileContent: String = "",
    val newFileName: String = "notes.txt",
    val statusMessage: String = "",
)
