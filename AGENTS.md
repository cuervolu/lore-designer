## Project structure

This is a Kotlin/JVM multi-module Gradle project.

Current modules:

```text
app
core
workspace
platform
editor
ui
```

Respect module boundaries and the existing dependency graph. Do not introduce new Gradle modules unless explicitly requested.

Keep `app` focused on application startup, dependency assembly, and desktop integration. Business and domain logic must live in the appropriate lower-level module.

## Dependency inspection

When you need to inspect a Gradle dependency, its API, or its implementation, use `ksrc` instead of browsing Gradle caches manually.

Start with:

```shell
ksrc --help
```

Useful commands:

```shell
ksrc deps --project .
ksrc search "SomeType|someFunction" --project . --artifact artifact-name
```

Prefer inspecting the actual dependency source over guessing APIs.

## Gradle and verification

Run Gradle commands from the repository root.

Prefer machine-friendly output:

```shell
./gradlew --quiet --console=plain <task>
```

Before handoff, run the relevant checks for the affected modules and ensure the project compiles.

Common commands:

```shell
./gradlew --quiet --console=plain spotlessApply
./gradlew --quiet --console=plain spotlessCheck
./gradlew --quiet --console=plain build
./gradlew --quiet --console=plain test
./gradlew --quiet --console=plain :app:run
```

Fix compilation and test failures caused by your changes before finishing.

Do not modify Gradle infrastructure, versions, repositories, plugins, or dependency choices unless the task requires it.

## Testing

Place tests in the module that owns the behavior:

```text
<module>/src/test/kotlin
```

Mirror the production package structure where practical.

Tests should cover meaningful behavior, state transitions, failures, regressions, and edge cases. Avoid trivial assertions and tests that only verify mock configuration.

Prefer small controllable fakes over unnecessary mocking or external resources.

Run affected tests before handoff.

## Kotlin conventions

Prefer clear, idiomatic Kotlin.

Use coroutines with `suspend` and `Flow` for asynchronous APIs where appropriate. Avoid callback-heavy and Rx-style APIs unless required by an external library.

The project standards are:

* Koin for dependency injection
* Kermit for logging
* `kotlinx.serialization` for serialization
* Compose for the desktop UI
* Compose Unstyled for shared UI primitives

Do not introduce competing frameworks without explicit justification or instruction.

Do not use `println` for application logging.

## Comments

Do not write comments that merely describe what the code already says.

Comments should explain non-obvious constraints, tradeoffs, compatibility requirements, or reasons behind unusual implementation decisions.

Prefer clearer code over explanatory comments.

## Scope discipline

Keep changes focused on the requested task.

Do not perform unrelated refactors, rename unrelated files, reorganize packages, add abstractions for hypothetical future requirements, or implement features beyond the requested scope.

Follow existing patterns before introducing new ones.

When a task depends on an unfamiliar library, inspect its source or documentation before implementing against it.

## Git conventions

Branch names should use:

```text
<type>/<slug>
```

Preferred types:

```text
feature
bugfix
refactor
chore
docs
test
```

Examples:

```text
feature/workspace-loader
bugfix/document-save-conflict
refactor/editor-boundary
chore/gradle-cleanup
```

Keep branches small and focused on a single intent.
