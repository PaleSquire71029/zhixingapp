# Backdrop 2.0.1 compatibility spike

> Status: waiting for GitHub Actions PR validation
> Branch: \`experiment/backdrop-2.0.1-compat\`
> Pull request: #4 (draft)
> This is an isolated dependency test. It is not production integration.

## Candidate under test

- Dependency: \`io.github.kyant0:backdrop:2.0.1\`
- Upstream license: Apache-2.0
- Kotlin compiler plugin probe: 2.4.21
- AGP: 9.1.1 (unchanged)
- Gradle Wrapper: 9.3.1 (unchanged)
- compileSdk: 37
- CI installs Android platform 37; build-tools remain 36.0.0 unless CI proves otherwise.

Kotlin's compatibility table lists KGP 2.4.20–2.4.21 as fully supported with Gradle 7.6.3–9.7.0 and AGP 8.5.2–9.3.1. AGP 9.1.1 supports API 37. The purpose of this combination is to avoid a simultaneous AGP and Gradle upgrade while testing the Backdrop 2.0.1 metadata.

## What this spike changes

- Updates the Compose compiler plugin version for the test branch only.
- Adds the pinned Backdrop artifact to the app module.
- Adds \`BackdropCompatibilityProbe.kt\`, an unused Composable that references:
  - \`rememberLayerBackdrop\`
  - \`Modifier.layerBackdrop\`
  - \`Modifier.drawBackdrop\`
  - \`blur\`
  - \`lens\`
- Updates the CI SDK install to API 37.

The probe is intentionally not called by MainActivity and does not affect the visible app.

## What must pass

1. Gradle resolves the Android artifact and all transitive dependencies.
2. Kotlin compilation accepts the library metadata and the referenced public API.
3. There are no duplicate Compose classes or incompatible Compose runtime artifacts.
4. \`:app:testDebugUnitTest\` passes.
5. \`:app:assembleDebug\` passes.

## Decision rule

- If every check passes, this is a viable dependency candidate, not yet approved for production UI.
- If a dependency conflict or toolchain error occurs, record the exact cause and test the smallest correction.
- Do not merge this draft PR before results are reviewed.
- Do not copy the test configuration to main before compatibility is proven.
