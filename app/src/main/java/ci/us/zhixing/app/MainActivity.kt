package ci.us.zhixing.app

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.theme.ZhixingLightColors
import ci.us.zhixing.app.ui.theme.ZhixingSpacing
import ci.us.zhixing.app.R

class MainActivity : ComponentActivity() {
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
            ZhixingApp()
        }
    }
}

@Composable
private fun ZhixingApp() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val destinations = AppDestinations.all
    val selectedDestination = destinations[selectedIndex]

    MaterialTheme(colorScheme = ZhixingLightColors) {
        Scaffold(
            containerColor = ZhixingLightColors.background,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                ) {
                    destinations.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = {
                                Text(
                                    text = destination.symbol,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            label = { Text(destination.label) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ZhixingLightColors.primary,
                                selectedTextColor = ZhixingLightColors.primary,
                                unselectedIconColor = ZhixingLightColors.onSurfaceVariant,
                                unselectedTextColor = ZhixingLightColors.onSurfaceVariant,
                                indicatorColor = ZhixingLightColors.primaryContainer,
                            ),
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
                    .padding(horizontal = ZhixingSpacing.pageHorizontal)
                    .padding(top = ZhixingSpacing.pageVertical, bottom = ZhixingSpacing.pageVertical),
                verticalArrangement = Arrangement.spacedBy(ZhixingSpacing.section),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ZhixingSpacing.headerGap),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.zhixing_logo),
                            contentDescription = "知行图标",
                            modifier = Modifier.height(54.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(ZhixingSpacing.tight)) {
                            Text(
                                text = "知行",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = ZhixingLightColors.onBackground,
                            )
                            Text(
                                text = "CALCULATOR  ×  AI",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.5.sp,
                                color = ZhixingLightColors.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        color = ZhixingLightColors.primaryContainer,
                        shape = CircleShape,
                    ) {
                        Text(
                            text = "原生预览",
                            modifier = Modifier.padding(horizontal = ZhixingSpacing.badgeHorizontal, vertical = ZhixingSpacing.badgeVertical),
                            style = MaterialTheme.typography.labelMedium,
                            color = ZhixingLightColors.onPrimaryContainer,
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, ZhixingLightColors.outline.copy(alpha = 0.7f)),
                ) {
                    Column(
                        modifier = Modifier.padding(ZhixingSpacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(ZhixingSpacing.cardContentGap),
                    ) {
                        Text(
                            text = selectedDestination.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = ZhixingLightColors.secondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = selectedDestination.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = ZhixingLightColors.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = selectedDestination.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ZhixingLightColors.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        )
                        Spacer(modifier = Modifier.height(ZhixingSpacing.hairlineGap))
                        Text(
                            text = "基础界面已建立 · 功能按小步迁移",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZhixingLightColors.primary,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(ZhixingSpacing.groupGap)) {
                    Text(
                        text = "本阶段先完成基础",
                        style = MaterialTheme.typography.titleMedium,
                        color = ZhixingLightColors.onBackground,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FeatureLine(title = "原生 Android 工程", detail = "Kotlin + Jetpack Compose")
                    FeatureLine(title = "知行浅色设计基线", detail = "先稳定布局与文字层级")
                    FeatureLine(title = "逐步迁移", detail = "液态玻璃、计算和 AI 分别验收")
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
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ZhixingLightColors.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ZhixingSpacing.featureHorizontal, vertical = ZhixingSpacing.featureVertical),
            verticalArrangement = Arrangement.spacedBy(ZhixingSpacing.tight),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = ZhixingLightColors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = ZhixingLightColors.onSurfaceVariant,
            )
        }
    }
}
