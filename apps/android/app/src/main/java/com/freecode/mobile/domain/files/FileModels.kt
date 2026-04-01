package com.freecode.mobile.domain.files

enum class FileNodeType {
    FILE,
    DIRECTORY,
}

data class FileNode(
    val path: String,
    val name: String,
    val type: FileNodeType,
    val depth: Int,
    val isReadable: Boolean,
    val isWritable: Boolean,
)
