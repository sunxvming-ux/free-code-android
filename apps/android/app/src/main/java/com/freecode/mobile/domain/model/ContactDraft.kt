package com.freecode.mobile.domain.model

data class ContactDraft(
    val name: String = "",
    val systemPrompt: String = "You are an Android AI assistant that can manage files and tasks.",
    val description: String = "",
    val providerKind: ProviderKind = ProviderKind.OPENAI,
    val model: String = "gpt-5.4",
    val permissionLevel: PermissionLevel = PermissionLevel.WORKSPACE,
    val workspaceName: String = "",
    val workspacePath: String = "",
)
