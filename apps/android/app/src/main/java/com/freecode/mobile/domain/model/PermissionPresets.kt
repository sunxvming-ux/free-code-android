package com.freecode.mobile.domain.model

fun permissionPreset(level: PermissionLevel): PermissionProfile {
    return when (level) {
        PermissionLevel.SANDBOX -> PermissionProfile(
            level = level,
            toolPolicy = ToolPolicy(
                allowShell = false,
                allowFilesystemRead = true,
                allowFilesystemWrite = false,
                allowNetwork = true,
                allowPluginInstall = false,
                allowRootExecution = false,
            ),
            allowedPaths = emptyList(),
        )
        PermissionLevel.WORKSPACE -> PermissionProfile(
            level = level,
            toolPolicy = ToolPolicy(
                allowShell = false,
                allowFilesystemRead = true,
                allowFilesystemWrite = true,
                allowNetwork = true,
                allowPluginInstall = false,
                allowRootExecution = false,
            ),
            allowedPaths = emptyList(),
        )
        PermissionLevel.EXTENDED -> PermissionProfile(
            level = level,
            toolPolicy = ToolPolicy(
                allowShell = true,
                allowFilesystemRead = true,
                allowFilesystemWrite = true,
                allowNetwork = true,
                allowPluginInstall = true,
                allowRootExecution = false,
            ),
            allowedPaths = emptyList(),
        )
        PermissionLevel.ROOT -> PermissionProfile(
            level = level,
            toolPolicy = ToolPolicy(
                allowShell = true,
                allowFilesystemRead = true,
                allowFilesystemWrite = true,
                allowNetwork = true,
                allowPluginInstall = true,
                allowRootExecution = true,
            ),
            allowedPaths = emptyList(),
        )
    }
}
