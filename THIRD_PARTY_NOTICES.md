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
