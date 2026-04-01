# free-code Android Shell

This module is the native Android shell for the repository.

## Scope of the scaffold
- Jetpack Compose application shell
- Bottom navigation with Messages / Contacts / Files / Settings
- Kotlin domain models for AI contacts, workspaces, permissions, and providers
- CI-ready Gradle structure

## Next steps
1. Replace fake repository data with Room-backed persistence.
2. Add Android bridges for filesystem, root shell, and provider secrets.
3. Wire real conversation execution to the shared engine contracts in `src/mobile/core/`.
4. Add editor and workspace browser implementations.
