package ci.us.zhixing.app.ui.lab

/**
 * 像素级背景采样区域。坐标相对于原始 GraphicsLayer 快照。
 */
internal data class BackdropCropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)

/**
 * 根据居中面板位置，从完整背景快照中计算安全的源裁切区域。
 *
 * 输入尺寸无效时返回 null；其他情况下保证源区域至少为 1×1，
 * 且不会越过快照边界。
 */
internal fun calculateBackdropCrop(
    imageWidth: Int,
    imageHeight: Int,
    panelInset: Int,
    panelHeight: Int,
): BackdropCropRect? {
    if (imageWidth <= 0 || imageHeight <= 0 || panelHeight <= 0) return null

    val safeInset = panelInset.coerceAtLeast(0)
    val left = safeInset.coerceIn(0, imageWidth - 1)
    val top = ((imageHeight - panelHeight) / 2).coerceIn(0, imageHeight - 1)
    val width = (imageWidth - safeInset * 2)
        .coerceAtLeast(1)
        .coerceAtMost(imageWidth - left)
    val height = panelHeight
        .coerceAtLeast(1)
        .coerceAtMost(imageHeight - top)

    return BackdropCropRect(
        left = left,
        top = top,
        width = width,
        height = height,
    )
}
