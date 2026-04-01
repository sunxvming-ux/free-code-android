# free-code Android Shell

This module is the native Android shell for the repository.

## Scope of the scaffold
- Jetpack Compose application shell
- Bottom navigation with Messages / Contacts / Files / Settings
- Kotlin domain models for AI contacts, workspaces, permissions, and providers
- Room persistence scaffold for contacts, providers, and conversation threads
- Android shell bridge contracts for standard and root-backed execution
- Workspace file service contracts and a local file tree preview implementation
- CI-ready Gradle structure

## Next steps
1. Add provider secret storage and encrypted key management.
2. Wire task execution to the Android shell bridge and permission profiles.
3. Connect real conversation execution to the shared engine contracts in `src/mobile/core/`.
4. Add editor and richer workspace browser implementations.
