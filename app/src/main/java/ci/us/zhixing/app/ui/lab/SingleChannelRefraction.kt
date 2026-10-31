package ci.us.zhixing.app.ui.lab

import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap

/**
 * 单通道边缘折射：RGB 共享同一采样坐标，不做色散。
 *
 * 只采样 Glass Lab 提供的局部快照，不涉及窗口级 backdrop。
 */
@RequiresApi(33)
internal class SingleChannelRefraction private constructor(
    private val runtimeShader: RuntimeShader,
) {
    val brush = ShaderBrush(runtimeShader)

    fun bindBackdrop(image: ImageBitmap, crop: BackdropCropRect) {
        val bitmapShader = BitmapShader(
            image.asAndroidBitmap(),
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP,
        )
        runtimeShader.setInputShader("backdrop", bitmapShader)
        runtimeShader.setFloatUniform(
            "sourceOrigin",
            crop.left.toFloat(),
            crop.top.toFloat(),
        )
        runtimeShader.setFloatUniform(
            "sourceSize",
            crop.width.toFloat(),
            crop.height.toFloat(),
        )
    }

    fun updateUniforms(widthPx: Float, heightPx: Float, strengthPx: Float) {
        runtimeShader.setFloatUniform("resolution", widthPx, heightPx)
        runtimeShader.setFloatUniform(
            "refractionPx",
            clampRefractionStrength(strengthPx),
        )
    }

    companion object {
        fun create(): SingleChannelRefraction =
            SingleChannelRefraction(RuntimeShader(SINGLE_CHANNEL_REFRACTION_AGSL))
    }
}

private const val SINGLE_CHANNEL_REFRACTION_AGSL = """
uniform shader backdrop;
uniform float2 resolution;
uniform float2 sourceOrigin;
uniform float2 sourceSize;
uniform float refractionPx;

half4 main(float2 fragCoord) {
    float2 safeResolution = float2(
        max(resolution.x, 1.0),
        max(resolution.y, 1.0)
    );

    // Keep the edge mask relative to the panel dimensions, but calculate the
    // displacement direction in pixel space so wide/tall panels stay isotropic.
    float2 lens = (fragCoord / safeResolution) * 2.0 - 1.0;
    float radius = length(lens);
    float edge = smoothstep(0.48, 1.15, radius);

    float2 pixelDirection = fragCoord - safeResolution * 0.5;
    float pixelRadius = length(pixelDirection);
    float2 direction = pixelDirection / max(pixelRadius, 0.0001);

    float2 displacedPoint = fragCoord - direction * edge * refractionPx;
    float2 samplePoint = sourceOrigin
        + (displacedPoint / safeResolution) * sourceSize;
    return backdrop.eval(samplePoint);
}
"""
