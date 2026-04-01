package com.freecode.mobile.domain.model

fun AiContact.toDraft(): ContactDraft = ContactDraft(
    name = name,
    systemPrompt = systemPrompt,
    description = description,
    providerKind = provider.kind,
    model = provider.model,
    permissionLevel = permissions.level,
    workspaceName = workspace.name,
    workspacePath = workspace.rootPath,
)
