package com.freecode.mobile.domain.files

import java.io.File

class LocalWorkspaceFileService : WorkspaceFileService {
    override suspend fun listTree(rootPath: String, maxDepth: Int): List<FileNode> {
        val root = File(rootPath)
        if (!root.exists()) return emptyList()
        val nodes = mutableListOf<FileNode>()

        fun walk(file: File, depth: Int) {
            if (depth > maxDepth) return
            nodes += FileNode(
                path = file.absolutePath,
                name = if (depth == 0) file.absolutePath else file.name,
                type = if (file.isDirectory) FileNodeType.DIRECTORY else FileNodeType.FILE,
                depth = depth,
                isReadable = file.canRead(),
                isWritable = file.canWrite(),
            )
            if (file.isDirectory) {
                file.listFiles()
                    ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                    ?.forEach { walk(it, depth + 1) }
            }
        }

        walk(root, 0)
        return nodes
    }

    override suspend fun createDirectory(path: String): Boolean = File(path).mkdirs()

    override suspend fun createFile(path: String, content: String): Boolean = runCatching {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        true
    }.getOrDefault(false)

    override suspend fun readText(path: String): String =
        File(path).takeIf { it.exists() }?.readText() ?: ""
}
