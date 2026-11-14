# Third-party notices

## AndroidLiquidGlass — Backdrop Catalog 1.0.6

- Upstream: https://github.com/Kyant0/AndroidLiquidGlass
- Pinned source: tag `1.0.6`, commit `896a94a3ade1cc1a940b92365f942a34971fecda`
- License: Apache License 2.0; see [the full license text](./THIRD_PARTY_LICENSES/AndroidLiquidGlass-1.0.6-LICENSE.txt).
- Ported source files:
  - `DampedDragAnimation.kt`
  - `InteractiveHighlight.kt`
  - `DragGestureInspector.kt`
  - `LiquidBottomTabs.kt`
  - `LiquidBottomTab.kt`
  - `LiquidButton.kt`
  - `LiquidToggle.kt`
- Zhixing modifications: upstream packages are relocated into `ci.us.zhixing.app.ui.glass.backdrop106`; component helper references point to the relocated P1 helpers. The official `LiquidButton.kt` component is relocated into the same isolated package; Zhixing adds a Shape and enabled-state parameter while retaining the upstream Catalog lens, damped touch response and drawing pipeline. The homepage material-settings entry uses the button through `ZhixingLiquidButton.kt`. The official `LiquidToggle.kt` is also relocated into the isolated package; Zhixing fixes its light palette, exposes enabled and touch-feedback controls, and adds Switch role/state/click semantics while retaining the upstream drag spring and combined Backdrop rendering. The Glass Lab edge-refraction control uses it via `ZhixingLiquidToggle.kt`. The Catalog tab UI is wrapped by `ZhixingCatalogBottomBar.kt` and now accepts the existing `GlassMaterialState` through `CatalogBottomBarMaterialParameters.kt`. Material rendering maps transparency, blur, vibrancy, refraction scale, dispersion, highlights, environment reflection and touch feedback to the Catalog surfaces. The P1 `DampedDragAnimation.kt` has one Zhixing extension: press/release scaling can be disabled without disabling drag tracking, velocity response or destination snapping. `LiquidBottomTabs.kt` retains the upstream drag physics and layer composition while adding a light-only material fallback and omitting unnecessary sampling layers when refraction is disabled. Each modified source file carries an attribution and modification notice.


## Material Symbols Rounded

- Upstream: https://github.com/google/material-design-icons
- License: Apache License 2.0.
- Used assets: `calculate`, `smart_toy`, `handyman`, `person`, `history`, and `tune` from the Material Symbols Rounded family, converted to Android VectorDrawable resources under `app/src/main/res/drawable/`.
- The six glyphs are used for calculator, 知行 AI, tools, My, history, and material settings navigation respectively.

## Symja symbolic mathematics core 3.2.0

- Upstream: https://github.com/axkr/symja_android_library
- Maven module: `org.matheclipse:matheclipse-core:3.2.0`.
- Pinned source tag: `v3.2.0`.
- License: LGPL-3.0 for `matheclipse-core`, `matheclipse-parser`, and `matheclipse-external`; the full license text is in [THIRD_PARTY_LICENSES/Symja-LGPL-3.0.txt](./THIRD_PARTY_LICENSES/Symja-LGPL-3.0.txt).
- Only the LGPL core module is used. GPL-licensed modules such as `matheclipse-api`, `matheclipse-gpl`, and `matheclipse-io` are not dependencies.
- Zhixing integration: a small Kotlin adapter validates the parsed expression tree against a mathematical allow-list, bounds input size, AST depth, exponent/factorial sizes, matrix dimensions and output size, and applies an evaluation timeout. It does not expose Symja's full scripting language or copy the GPL-licensed Android sample wrapper.
- Android dependency adjustments: core-library desugaring enabled; Guava is pinned to the Android flavor `33.8.0-android`; Jackson Databind is pinned to patched `2.21.7`; Log4j API is pinned to patched `2.25.5`. Review the resolved dependency graph and licenses again before public release.
- The Symja engine is an optional domain backend; existing tokenizer, parser, polynomial, rational-function and typed solver code remains intact and is not replaced by this dependency.
