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
- Zhixing modifications: upstream packages are relocated into `ci.us.zhixing.app.ui.glass.backdrop106`; component helper references point to the relocated P1 helpers. The Catalog tab UI is wrapped by `ZhixingCatalogBottomBar.kt`, which maps the existing Zhixing destination model and visual tokens without changing upstream gesture or lens algorithms. Each ported source file carries an attribution and modification notice.

The P1 helpers, P2.1 official tab components, and P2.2 Zhixing adapter remain isolated; they are not wired into the production UI yet. Subsequent P2 steps will add tests and a controlled production integration.
