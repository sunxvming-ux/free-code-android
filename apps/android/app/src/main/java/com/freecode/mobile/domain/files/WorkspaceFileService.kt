package com.freecode.mobile.domain.files

interface WorkspaceFileService {
    suspend fun listTree(rootPath: String, maxDepth: Int = 2): List<FileNode>
    suspend fun createDirectory(path: String): Boolean
    suspend fun createFile(path: String, content: String = ""): Boolean
    suspend fun readText(path: String): String
}
