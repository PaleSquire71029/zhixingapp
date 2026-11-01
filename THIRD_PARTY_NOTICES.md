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
- Zhixing modifications in this P1 step: the Kotlin package namespace was relocated to `ci.us.zhixing.app.ui.glass.backdrop106`; implementation behavior was otherwise retained. Each source file carries an attribution and modification notice.

The P1 helper classes and P2.1 tab components remain isolated under `ui/glass/backdrop106`; they are not wired into the production UI yet. Subsequent P2 steps will add the Zhixing adapter, tests, and controlled production integration.
