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

The port is isolated and is not wired into the production UI in P1. Integration is reserved for the subsequent bottom-navigation migration step.
