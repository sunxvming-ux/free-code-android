# Android Adaptation Architecture

## Goal
Adapt the repository into an Android-first client while preserving the current agent-oriented architecture: providers, tools, permissions, plugins, workspaces, and conversations remain first-class concepts.

## Recommended Topology
- `src/`: existing Bun/TypeScript CLI and core logic.
- `src/mobile/core/`: platform-neutral contracts that describe agents, permissions, workspaces, providers, and plugin metadata.
- `apps/android/`: native Android shell built with Kotlin + Jetpack Compose.
- `.github/workflows/`: CI workflows for Android assembly.

## Target Architecture
### 1. Engine Layer
Keep the project's current mental model:
- provider adapters
- tool execution
- permission evaluation
- plugin discovery
- conversation state
- workspace isolation

### 2. Android Platform Layer
Implement Android-specific bridges for:
- root / `su` shell execution
- app-private and external storage workspaces
- secure key storage
- background task lifecycle
- file tree and editor state

### 3. Presentation Layer
Bottom navigation uses four tabs:
1. Messages
2. Contacts
3. Files
4. Settings

Each AI contact maps to its own workspace, permission profile, provider config, and conversation threads.

## Permission Model
Recommended levels:
- `SANDBOX`: restricted app storage only
- `WORKSPACE`: assigned workspace only
- `EXTENDED`: selected external paths and tools
- `ROOT`: `su`-backed execution with explicit toggle

## Plugin Strategy
Split plugins into two categories:
- **Portable plugins**: prompts, workflows, model adapters, file analyzers
- **Android bridge plugins**: shell, filesystem, intent, terminal, root bridge

## Phased Delivery
### Phase 1
Scaffold Android app, navigation, domain models, provider settings, and contact management.

### Phase 2
Add workspace manager, file browser/editor, and permission enforcement.

### Phase 3
Add tool execution bridge, root execution, plugin runtime, and model/provider integration.

### Phase 4
Add richer editor features, background jobs, and Android-specific automation.
