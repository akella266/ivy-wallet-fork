# Repository Guidelines

## Project Structure & Module Organization
- `app/` holds the Android application module and entry point; most Compose screens and navigation handlers live here.
- `screen/` contains Compose-heavy feature modules (e.g., `screen/sms`, `screen/transaction`); treat each folder as a self-contained screen boundary with its own view model and navigation actions.
- `shared/` and `widget/` host reusable UI components, data models, and helper logic shared across screens (`shared/data`, `shared/ui`, etc.).
- `buildSrc/`, `gradle/`, and `scripts/` contain Gradle convention plugins, tooling scripts, and CI helpers; keep changes there limited to build-related concerns.
- `docs/` and `fastlane/` store documentation, release notes, and deployment scripts (Fastlane lanes for Play Store uploads).

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a debug APK; use this before manual QA on device/emulator.
- `./gradlew test` runs all JVM unit tests; it must pass before pushing.
- `./gradlew lint` + `./gradlew detekt` + `./gradlew ktlintCheck` enforce Compose/lint rules and Kotlin style.
- `./gradlew :app:paparazzi` executes screenshot tests tracked by Paparazzi; run when adding new UI states.
- `./gradlew :app:connectedDebugAndroidTest` (CI) executes instrumentation tests; reserve for device-focused work.

## Coding Style & Naming Conventions
- Pure Kotlin/Compose code with 4-space indentation; prefer expression-style functions and single-expression returns.
- Use `camelCase` for functions and variables, `PascalCase` for composables/classes, `snake_case` only for resource files.
- Keep Compose previews near their component definitions and reuse `@Composable` helper functions for repeated layouts.
- Run formatting linters (`ktlint`, `detekt`, Slack?s Compose lints) and fix violations before committing.

## Testing Guidelines
- Unit tests rely on JUnit4 + Kotest assertions; name them `FeatureNameTest` and its methods with `should` clauses (e.g., `fun shouldShowEmptyState()`).
- Paparazzi screenshot specs live next to the feature under test; rerun after UI changes.
- Run `./gradlew test` locally before pushing; add Kotlin-specific mocks or helpers under `shared/test` if shared.

## Commit & Pull Request Guidelines
- Commit messages follow the existing short, present-tense style (`Fix empty state`, `Improve SMS list screen`); keep each message ? 50 characters and imperative when possible.
- Before claiming an issue, comment exactly `"I'm on it"` on the GitHub thread to trigger automation.
- PRs must target `main`, include a descriptive summary, link to the tracked issue, and attach screenshots if UI changes are involved.
- Mention any manual verification steps (device, emulator) in the PR body so reviewers can reproduce.

## Configuration & Tooling Tips
- Sync Gradle version catalogs from `gradle/libs.versions.toml` to avoid dependency drift.
- Keep `local.properties` secrets (SDK path, keystore) out of commits.
- Consult `docs/Guidelines.md` and the linked `docs/guidelines/*` references before starting a feature for architecture or testing expectations.
