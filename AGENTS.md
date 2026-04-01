# Repository Guidelines

## Project Structure & Module Organization
- `src/` contains the Bun + TypeScript CLI application. Key areas include `entrypoints/` for startup, `commands/` for slash-command handlers, `tools/` for agent tools, `components/` and `screens/` for Ink UI, and `services/`, `state/`, `utils/`, `skills/`, and `plugins/` for runtime support.
- `scripts/build.ts` is the main build pipeline and feature-flag entrypoint.
- `assets/` holds static assets such as `assets/screenshot.png`.
- Root docs (`README.md`, `FEATURES.md`, `changes.md`) explain feature flags, usage, and reconstruction notes.

## Build, Test, and Development Commands
- `bun run build` ！ build the default CLI output (`./cli`).
- `bun run build:dev` ！ build a dev-stamped binary (`./cli-dev`).
- `bun run build:dev:full` ！ build the dev binary with the full experimental feature set.
- `bun run compile` ！ emit the compiled binary to `./dist/cli`.
- `bun run dev` ！ run the CLI directly from source for iterative development.
- `./cli -p "prompt"` ！ quick smoke test for one-shot execution after a build.

## Coding Style & Naming Conventions
- Follow the surrounding file style: TypeScript ESM, 2-space indentation, single quotes, and minimal formatting churn.
- Use `PascalCase` for React/Ink components (`REPL.tsx`), `camelCase` for functions and utilities, and descriptive kebab-case for command folders under `src/commands/`.
- Keep new modules close to the subsystem they extend; avoid adding cross-cutting helpers until reuse is clear.

## Testing Guidelines
- This snapshot does not include a dedicated automated test suite. Validate changes with targeted builds and manual CLI smoke tests.
- At minimum, run the relevant build command and exercise the affected flow via `bun run dev` or the built `./cli` binary.
- For feature-flag work, note the exact flag combination used (for example `--feature=ULTRAPLAN`).

## Commit & Pull Request Guidelines
- Git history is not bundled in this snapshot, so follow the repository's documented examples: short, imperative commits with prefixes like `feat:`, `fix:`, or `docs:`.
- PRs should describe the user-visible change, list build/manual verification steps, link related issues, and include screenshots or terminal output when UI or CLI behavior changes.
- If you touch experimental flags, update `FEATURES.md` or `changes.md` when the status or behavior changes.
