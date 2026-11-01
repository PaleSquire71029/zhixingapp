package ci.us.zhixing.app

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.activity.compose.BackHandler
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.glass.BackdropHost
import ci.us.zhixing.app.ui.glass.DEFAULT_HOME_GLASS_MATERIAL
import ci.us.zhixing.app.ui.glass.GlassMaterialSettingsStore
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingLiquidBottomBar
import ci.us.zhixing.app.ui.glass.ZhixingLiquidBottomBarPlacement
import ci.us.zhixing.app.ui.lab.GlassLabScreen
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import ci.us.zhixing.app.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val glassMaterialSettingsStore by lazy {
        GlassMaterialSettingsStore(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Target SDK 36 uses edge-to-edge system bars. Keep icons dark on the
        // light Zhixing canvas and let Compose content respect safe drawing insets.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            ZhixingApp(
                lifecycle = lifecycle,
                materialSettingsStore = glassMaterialSettingsStore,
            )
        }
    }
}

@Composable
private fun ZhixingApp(
    lifecycle: Lifecycle,
    materialSettingsStore: GlassMaterialSettingsStore,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var showGlassLab by rememberSaveable { mutableStateOf(false) }
    val persistedMaterial by materialSettingsStore.material.collectAsState(
        initial = DEFAULT_HOME_GLASS_MATERIAL,
    )
    var previewMaterial by remember { mutableStateOf<GlassMaterialState?>(null) }
    val glassMaterial = previewMaterial ?: persistedMaterial
    val persistenceScope = rememberCoroutineScope()

    // Once DataStore confirms the same value, discard the temporary live preview.
    LaunchedEffect(persistedMaterial) {
        if (previewMaterial == persistedMaterial) {
            previewMaterial = null
        }
    }
    val destinations = AppDestinations.all
    val selectedDestination = destinations[selectedIndex]

    ZhixingTheme {
        BackHandler(enabled = showGlassLab) {
            showGlassLab = false
        }
        if (showGlassLab) {
            GlassLabScreen(
                onBack = { showGlassLab = false },
                lifecycle = lifecycle,
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
        } else {
            BackdropHost(
                modifier = Modifier.fillMaxSize(),
            ) { backdrop ->
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ZhixingLiquidBottomBarPlacement {
                                ZhixingLiquidBottomBar(
                                    backdrop = backdrop,
                                    material = glassMaterial,
                                    destinations = destinations,
                                    selectedIndex = selectedIndex,
                                    onDestinationSelected = { selectedIndex = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(68.dp),
                                )
                            }
                        }
                    },
                ) { innerPadding ->
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

                            Surface(
                                modifier = Modifier.clickable(
                                    role = Role.Button,
                                    onClick = { showGlassLab = true },
                                ),
                                color = ZhixingTheme.colors.primaryContainer,
                                shape = CircleShape,
                            ) {
                                Text(
                                    text = "材质设置",
                                    modifier = Modifier.padding(
                                        horizontal = ZhixingTheme.spacing.badgeHorizontal,
                                        vertical = ZhixingTheme.spacing.badgeVertical,
                                    ),
                                    style = ZhixingTheme.type.badge,
                                    color = ZhixingTheme.colors.onPrimaryContainer,
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shape = ZhixingTheme.shapes.hero,
                            border = BorderStroke(
                                1.dp,
                                ZhixingTheme.colors.outline.copy(alpha = 0.7f),
                            ),
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
                            )
                            FeatureLine(
                                title = "知行浅色设计基线",
                                detail = "先稳定布局与文字层级",
                            )
                            FeatureLine(
                                title = "逐步迁移",
                                detail = "液态玻璃、计算和 AI 分别验收",
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureLine(title: String, detail: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.78f),
        shape = ZhixingTheme.shapes.feature,
        border = BorderStroke(1.dp, ZhixingTheme.colors.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ZhixingTheme.spacing.featureHorizontal, vertical = ZhixingTheme.spacing.featureVertical),
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
