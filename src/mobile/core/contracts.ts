export type PermissionLevel = 'SANDBOX' | 'WORKSPACE' | 'EXTENDED' | 'ROOT'

export type ModelProviderKind =
  | 'anthropic'
  | 'openai'
  | 'bedrock'
  | 'vertex'
  | 'foundry'
  | 'custom'

export interface WorkspaceBinding {
  id: string
  name: string
  rootPath: string
  writableRoots: string[]
  externalRoots: string[]
}

export interface ToolPolicy {
  allowShell: boolean
  allowFilesystemRead: boolean
  allowFilesystemWrite: boolean
  allowNetwork: boolean
  allowPluginInstall: boolean
  allowRootExecution: boolean
}

export interface PermissionProfile {
  level: PermissionLevel
  toolPolicy: ToolPolicy
  allowedPaths: string[]
  deniedPaths: string[]
}

export interface ProviderConfig {
  id: string
  kind: ModelProviderKind
  baseUrl?: string
  model: string
  apiKeyRef?: string
  headers?: Record<string, string>
  enabledPlugins: string[]
}

export interface AiContact {
  id: string
  name: string
  avatar?: string
  systemPrompt: string
  description?: string
  provider: ProviderConfig
  workspace: WorkspaceBinding
  permissions: PermissionProfile
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface ConversationThread {
  id: string
  aiId: string
  title: string
  lastMessagePreview: string
  updatedAt: string
  pinned: boolean
}

export interface PluginDescriptor {
  id: string
  displayName: string
  version: string
  category: 'portable' | 'android-bridge'
  enabled: boolean
}

export interface AndroidExecutionRequest {
  aiId: string
  workspaceId: string
  command: string
  cwd?: string
  permissionLevel: PermissionLevel
}
