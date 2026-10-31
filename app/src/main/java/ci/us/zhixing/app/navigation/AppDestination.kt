package ci.us.zhixing.app.navigation

internal data class AppDestination(
    val route: String,
    val label: String,
    val symbol: String,
    val title: String,
    val description: String,
)

internal object AppDestinations {
    val all = listOf(
        AppDestination(
            route = "calculator",
            label = "计算",
            symbol = "⌗",
            title = "让计算回归清晰",
            description = "本地计算核心会逐项迁移。标准计算、科学计算、方程、不等式和函数工具都将保留独立入口。",
        ),
        AppDestination(
            route = "ai",
            label = "知行AI",
            symbol = "✦",
            title = "让计算与理解连在一起",
            description = "原生 AI 对话、连续追问、图片识题和计算上下文联动，将在后续小步骤中接入。",
        ),
        AppDestination(
            route = "tools",
            label = "工具",
            symbol = "▦",
            title = "把复杂问题拆开处理",
            description = "数学与化学工具会逐项迁移，每个工具独立实现、独立测试，不堆进一个大页面。",
        ),
        AppDestination(
            route = "history",
            label = "历史",
            symbol = "↶",
            title = "把重要过程留存下来",
            description = "本地计算记录与 AI 云端对话将在这里统一查看，同时保留各自的数据来源和权限边界。",
        ),
    )
}
