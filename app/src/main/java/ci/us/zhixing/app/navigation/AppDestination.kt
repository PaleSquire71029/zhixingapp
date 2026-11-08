package ci.us.zhixing.app.navigation

internal enum class AppNavigationIcon {
    CALCULATOR,
    AI,
    TOOLS,
    ME,
    HISTORY,
    MATERIAL,
}

internal data class AppDestination(
    val route: String,
    val label: String,
    val icon: AppNavigationIcon,
    val title: String,
    val description: String,
)

internal object AppDestinations {
    val all = listOf(
        AppDestination(
            route = "calculator",
            label = "计算",
            icon = AppNavigationIcon.CALCULATOR,
            title = "让计算回归清晰",
            description = "本地计算核心会逐项迁移。标准计算、科学计算、方程、不等式和函数工具都将保留独立入口。",
        ),
        AppDestination(
            route = "ai",
            label = "知行 AI",
            icon = AppNavigationIcon.AI,
            title = "让计算与理解连在一起",
            description = "原生 AI 对话、连续追问、图片识题和计算上下文联动，将在后续小步骤中接入。",
        ),
        AppDestination(
            route = "tools",
            label = "工具",
            icon = AppNavigationIcon.TOOLS,
            title = "把复杂问题拆开处理",
            description = "数学与化学工具会逐项迁移，每个工具独立实现、独立测试，不堆进一个大页面。",
        ),
        AppDestination(
            route = "my",
            label = "我的",
            icon = AppNavigationIcon.ME,
            title = "本地空间与外观设置",
            description = "历史记录、材质设置和应用信息集中在这里。",
        ),
    )
}
