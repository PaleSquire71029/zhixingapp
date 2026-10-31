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
- compileSdk: 36 (same as production baseline)
- CI installs Android platform 36 and build-tools 36.0.0.
- Candidate 1.0.6 source configures compileSdk 36, uses AndroidX Compose 1.10.3 and is built as a native Android library.
- API 37 was attempted first, but the current GitHub Runner SDK repository returned `Failed to find package 'platforms;android-37'` before Gradle started. This is an SDK package availability issue, not evidence of Backdrop incompatibility. The spike therefore stays on API 36 to isolate the actual AAR/Compose compatibility question.

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


## Retry after SDK package lookup failure

The first PR check (run #36, 2026-10-31) stopped before Gradle because the runner could not find `platforms;android-37`. No dependency or Kotlin compilation took place in that run.

The test branch has now been adjusted to compile against SDK 36, matching the app's stable baseline. This keeps the test focused on the Backdrop AAR's actual metadata, Compose dependencies, Kotlin compiler compatibility and public API.

The next PR check is the first meaningful compile attempt for Backdrop 2.0.1.

## Candidate change: Backdrop 1.0.6

The 2.x line was not suitable for this immediate integration because its published Android artifacts require compileSdk 37 and JetBrains Compose 1.12.0. The 1.0.6 release source instead uses AndroidX Compose 1.10.3 and compileSdk 36. The test branch now switches to 1.0.6 while retaining the Kotlin 2.4.21 / AGP 9.1.1 / Gradle 9.3.1 app toolchain. This is a better-fit candidate for the current application, subject to CI compilation and tests.
