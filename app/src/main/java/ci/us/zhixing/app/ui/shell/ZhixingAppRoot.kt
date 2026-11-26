package ci.us.zhixing.app.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.navigation.AppNavigationState
import ci.us.zhixing.app.navigation.AppRootRoute
import ci.us.zhixing.app.navigation.AppSecondaryRoute
import ci.us.zhixing.app.ui.glass.BackdropHost
import ci.us.zhixing.app.ui.glass.DEFAULT_HOME_GLASS_MATERIAL
import ci.us.zhixing.app.ui.glass.GlassMaterialSettingsStore
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.lab.GlassLabScreen
import ci.us.zhixing.app.ui.history.HistoryScreen
import ci.us.zhixing.app.ui.my.MyScreen
import ci.us.zhixing.app.ui.units.ToolsDirectoryScreen
import ci.us.zhixing.app.ui.units.UnitConverterScreen
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import ci.us.zhixing.app.R
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.launch

@Composable
internal fun ZhixingAppRoot(
    materialSettingsStore: GlassMaterialSettingsStore,
) {
    // Keep both saveable calls at the root Shell level and in their original
    // order so Compose's positional saveable-state slots remain stable.
    val rootRouteState = rememberSaveable(stateSaver = AppRootRoute.saver) {
        mutableStateOf(AppRootRoute.CALCULATOR)
    }
    val secondaryRouteState = rememberSaveable(stateSaver = AppSecondaryRoute.saver) {
        mutableStateOf<AppSecondaryRoute?>(null)
    }
    val saveableStateHolder = rememberSaveableStateHolder()
    val navigationState = remember(rootRouteState, secondaryRouteState) {
        AppNavigationState(
            rootRouteState = rootRouteState,
            secondaryRouteState = secondaryRouteState,
        )
    }
    // Do not draw a default-material frame before DataStore has restored the
    // user's actual optical values. That first-frame swap used to make every
    // backdrop surface visibly flash when the app cold-started.
    val persistedMaterial by produceState<GlassMaterialState?>(
        initialValue = null,
        key1 = materialSettingsStore,
    ) {
        materialSettingsStore.material.collect { restored ->
            value = restored
        }
    }
    var previewMaterial by remember { mutableStateOf<GlassMaterialState?>(null) }
    val persistenceScope = rememberCoroutineScope()

    if (persistedMaterial == null) {
        ZhixingTheme {
            ZhixingStartupSurface()
        }
        return
    }

    val restoredMaterial = checkNotNull(persistedMaterial)
    val glassMaterial = previewMaterial ?: restoredMaterial

    LaunchedEffect(restoredMaterial) {
        if (previewMaterial == restoredMaterial) {
            previewMaterial = null
        }
    }
    val destinations = AppDestinations.all
    val selectedIndex = navigationState.selectedRootIndex
    val selectedDestination = destinations[selectedIndex]

    ZhixingTheme {
        BackHandler(enabled = navigationState.canNavigateBack) {
            navigationState.closeSecondary()
        }
        AnimatedContent(
            targetState = navigationState.secondaryRoute,
            transitionSpec = {
                (
                    fadeIn(tween(230)) +
                        slideInHorizontally(tween(280)) { it / 9 } +
                        scaleIn(initialScale = 0.985f, animationSpec = tween(280))
                    ) togetherWith (
                    fadeOut(tween(170)) +
                        slideOutHorizontally(tween(220)) { -it / 14 } +
                        scaleOut(targetScale = 0.99f, animationSpec = tween(220))
                    )
            },
            label = "zhixingSecondaryPageTransition",
        ) { pageRoute ->
        when (pageRoute) {
            AppSecondaryRoute.MATERIAL -> saveableStateHolder.SaveableStateProvider(
                AppSecondaryRoute.MATERIAL.saveableStateKey,
            ) {
                GlassLabScreen(
                    onBack = { navigationState.closeSecondary() },
                    initialMaterial = glassMaterial,
                    onMaterialChange = { material ->
                        previewMaterial = material
                    },
                    onMaterialCommit = { material ->
                        previewMaterial = material
                        persistenceScope.launch {
                            materialSettingsStore.save(material)
                        }
                    },
                    onMaterialReset = {
                        previewMaterial = DEFAULT_HOME_GLASS_MATERIAL
                        persistenceScope.launch {
                            materialSettingsStore.resetToDefaults()
                        }
                    },
                )
            }
            AppSecondaryRoute.HISTORY -> saveableStateHolder.SaveableStateProvider(
                AppSecondaryRoute.HISTORY.saveableStateKey,
            ) {
                HistoryScreen(onBack = { navigationState.closeSecondary() })
            }
            AppSecondaryRoute.UNIT_CONVERTER -> saveableStateHolder.SaveableStateProvider(
                AppSecondaryRoute.UNIT_CONVERTER.saveableStateKey,
            ) {
                BackdropHost(modifier = Modifier.fillMaxSize()) { backdrop ->
                    UnitConverterScreen(
                        backdrop = backdrop,
                        material = glassMaterial,
                        onBack = { navigationState.closeSecondary() },
                    )
                }
            }
            else -> {
                BackdropHost(
                modifier = Modifier.fillMaxSize(),
            ) { backdrop ->
                // Content containers stay optically calm: preserve the user's
                // transparency, blur and edge-highlight choices, but do not
                // refract or disperse text and formulas.
                val contentGlassMaterial = glassMaterial.copy(
                    refractionEnabled = false,
                    chromaticDispersionEnabled = false,
                )
                // Static feature rows keep real backdrop translucency and
                // edge highlights, but do not each run a full blur/vibrancy
                // pass during the first home-frame render.
                val staticFeatureMaterial = contentGlassMaterial.copy(
                    blurRadiusDp = 0f,
                    environmentReflectionEnabled = false,
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                    if (navigationState.rootRoute == AppRootRoute.MY) {
                        saveableStateHolder.SaveableStateProvider(AppRootRoute.MY.saveableStateKey) {
                            MyScreen(
                                backdrop = backdrop,
                                material = staticFeatureMaterial,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                onOpenHistory = {
                                    navigationState.openSecondary(AppSecondaryRoute.HISTORY)
                                },
                                onOpenMaterial = {
                                    navigationState.openSecondary(AppSecondaryRoute.MATERIAL)
                                },
                            )
                        }
                    } else if (navigationState.rootRoute == AppRootRoute.TOOLS) {
                        saveableStateHolder.SaveableStateProvider(AppRootRoute.TOOLS.saveableStateKey) {
                            ToolsDirectoryScreen(
                                backdrop = backdrop,
                                material = staticFeatureMaterial,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                onOpenUnitConverter = {
                                    navigationState.openSecondary(AppSecondaryRoute.UNIT_CONVERTER)
                                },
                            )
                        }
                    } else {
                        saveableStateHolder.SaveableStateProvider(navigationState.rootRoute.saveableStateKey) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
                            .padding(
                                top = ZhixingTheme.spacing.pageVertical,
                                bottom = ZhixingTheme.spacing.pageVertical,
                            ),
                        verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.section),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.headerGap),
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.zhixing_logo),
                                    contentDescription = "知行图标",
                                    modifier = Modifier.height(54.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight),
                                ) {
                                    Text(
                                        text = "知行",
                                        style = ZhixingTheme.type.brandTitle,
                                        color = ZhixingTheme.colors.onBackground,
                                    )
                                    Text(
                                        text = "CALCULATOR  ×  AI",
                                        style = ZhixingTheme.type.brandCaption,
                                        color = ZhixingTheme.colors.onSurfaceVariant,
                                    )
                                }
                            }

                        }

                        val heroShape = ZhixingGlassShapes.hero
                        ZhixingGlassSurface(
                            backdrop = backdrop,
                            material = contentGlassMaterial,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, heroShape, clip = false)
                                .clip(heroShape),
                            shape = heroShape,
                        ) {
                            Column(
                                modifier = Modifier.padding(ZhixingTheme.spacing.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(
                                    ZhixingTheme.spacing.cardContentGap,
                                ),
                            ) {
                                Text(
                                    text = selectedDestination.label,
                                    style = ZhixingTheme.type.sectionLabel,
                                    color = ZhixingTheme.colors.secondary,
                                )
                                Text(
                                    text = selectedDestination.title,
                                    style = ZhixingTheme.type.screenTitle,
                                    color = ZhixingTheme.colors.onSurface,
                                )
                                Text(
                                    text = selectedDestination.description,
                                    style = ZhixingTheme.type.body,
                                    color = ZhixingTheme.colors.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(ZhixingTheme.spacing.hairlineGap))
                                Text(
                                    text = "基础界面已建立 · 功能按小步迁移",
                                    style = ZhixingTheme.type.badge,
                                    color = ZhixingTheme.colors.primary,
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.groupGap),
                        ) {
                            Text(
                                text = "本阶段先完成基础",
                                style = ZhixingTheme.type.sectionTitle,
                                color = ZhixingTheme.colors.onBackground,
                            )
                            FeatureLine(
                                title = "原生 Android 工程",
                                detail = "Kotlin + Jetpack Compose",
                                backdrop = backdrop,
                                material = staticFeatureMaterial,
                            )
                            FeatureLine(
                                title = "知行浅色设计基线",
                                detail = "先稳定布局与文字层级",
                                backdrop = backdrop,
                                material = staticFeatureMaterial,
                            )
                            FeatureLine(
                                title = "逐步迁移",
                                detail = "液态玻璃、计算和 AI 分别验收",
                                backdrop = backdrop,
                                material = staticFeatureMaterial,
                            )
                        }

                    }
                        }
                    }
                    ZhixingDockOverlay(
                        backdrop = backdrop,
                        material = glassMaterial,
                        destinations = destinations,
                        selectedIndex = selectedIndex,
                        onDestinationSelected = navigationState::selectRoot,
                    )
                    }
                }
            }
            }
        }
        }
    }
}

@Composable
private fun FeatureLine(
    title: String,
    detail: String,
    backdrop: Backdrop,
    material: GlassMaterialState,
) {
    val shape = ZhixingGlassShapes.card
    ZhixingGlassSurface(
        backdrop = backdrop,
        material = material,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .clip(shape),
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ZhixingTheme.spacing.featureHorizontal,
                vertical = ZhixingTheme.spacing.featureVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight),
        ) {
            Text(
                text = title,
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            Text(
                text = detail,
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}


/**
 * Same light gradient as BackdropHost, but without creating a Backdrop or
 * compiling glass effects while the first DataStore emission is pending.
 * Matching the final background prevents a white-to-glass launch flash.
 */
@Composable
private fun ZhixingStartupSurface() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val minDimension = size.minDimension
                val baseGradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF2F8FC),
                        Color(0xFFF3F1FA),
                        Color(0xFFFFF7EF),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                val upperGlow = Brush.radialGradient(
                    colors = listOf(Color(0x38A9D6EE), Color(0x00A9D6EE)),
                    center = Offset(size.width * 0.16f, size.height * 0.12f),
                    radius = minDimension * 0.78f,
                )
                val lowerGlow = Brush.radialGradient(
                    colors = listOf(Color(0x25C5B9E9), Color(0x00C5B9E9)),
                    center = Offset(size.width * 0.88f, size.height * 0.82f),
                    radius = minDimension * 0.72f,
                )
                val warmGlow = Brush.radialGradient(
                    colors = listOf(Color(0x1EDDC6A8), Color(0x00DDC6A8)),
                    center = Offset(size.width * 0.72f, size.height * 0.28f),
                    radius = minDimension * 0.58f,
                )
                onDrawBehind {
                    drawRect(baseGradient)
                    drawCircle(upperGlow, minDimension * 0.78f, Offset(size.width * 0.16f, size.height * 0.12f))
                    drawCircle(lowerGlow, minDimension * 0.72f, Offset(size.width * 0.88f, size.height * 0.82f))
                    drawCircle(warmGlow, minDimension * 0.58f, Offset(size.width * 0.72f, size.height * 0.28f))
                }
            },
    )
}
