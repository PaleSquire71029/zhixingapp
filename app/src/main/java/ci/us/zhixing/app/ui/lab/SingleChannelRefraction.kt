package ci.us.zhixing.app.ui.lab

import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap

/**
 * 单通道折射与轻量边缘高光。
 *
 * RGB 共享同一采样坐标，不做色散。高光使用独立 uniform 控制，
 * 即使折射强度设为 0，也可以单独显示边缘高光。
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

    fun updateUniforms(
        widthPx: Float,
        heightPx: Float,
        strengthPx: Float,
        rimWidthPx: Float,
        rimSoftnessPx: Float,
        rimStrength: Float,
        dispersionPx: Float,
    ) {
        runtimeShader.setFloatUniform("resolution", widthPx, heightPx)
        runtimeShader.setFloatUniform(
            "refractionPx",
            clampRefractionStrength(strengthPx),
        )
        runtimeShader.setFloatUniform("rimWidthPx", rimWidthPx.coerceAtLeast(0.1f))
        runtimeShader.setFloatUniform("rimSoftnessPx", rimSoftnessPx.coerceAtLeast(0.1f))
        runtimeShader.setFloatUniform("rimStrength", rimStrength.coerceIn(0f, 0.46f))
        runtimeShader.setFloatUniform(
            "dispersionPx",
            clampChromaticDispersionStrength(dispersionPx),
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
uniform float rimWidthPx;
uniform float rimSoftnessPx;
uniform float rimStrength;
uniform float dispersionPx;

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

    // Dispersion is intentionally sub-pixel and edge-masked. The red and blue
    // samples move in opposite directions; green remains on the refracted point.
    half4 color;
    if (dispersionPx > 0.001 && edge > 0.001) {
        float2 dispersionSampleOffset =
            (direction * edge * dispersionPx / safeResolution) * sourceSize;
        half4 redSample = backdrop.eval(samplePoint + dispersionSampleOffset);
        half4 greenSample = backdrop.eval(samplePoint);
        half4 blueSample = backdrop.eval(samplePoint - dispersionSampleOffset);
        color = half4(redSample.r, greenSample.g, blueSample.b, greenSample.a);
    } else {
        color = backdrop.eval(samplePoint);
    }

    // A restrained, directional white rim: strongest on the top and left.
    // The clip shape applied by Compose trims the rectangular mask to rounded corners.
    float leftDistance = max(fragCoord.x, 0.0);
    float rightDistance = max(safeResolution.x - fragCoord.x, 0.0);
    float topDistance = max(fragCoord.y, 0.0);
    float bottomDistance = max(safeResolution.y - fragCoord.y, 0.0);

    float leftRim = 1.0 - smoothstep(
        rimWidthPx,
        rimWidthPx + rimSoftnessPx,
        leftDistance
    );
    float rightRim = 1.0 - smoothstep(
        rimWidthPx,
        rimWidthPx + rimSoftnessPx,
        rightDistance
    );
    float topRim = 1.0 - smoothstep(
        rimWidthPx,
        rimWidthPx + rimSoftnessPx,
        topDistance
    );
    float bottomRim = 1.0 - smoothstep(
        rimWidthPx,
        rimWidthPx + rimSoftnessPx,
        bottomDistance
    );

    float rim = clamp(
        topRim * 0.92 +
        leftRim * 0.68 +
        bottomRim * 0.16 +
        rightRim * 0.12,
        0.0,
        1.0
    );
    half rimOpacity = half(clamp(rim * rimStrength, 0.0, 0.46));
    color.rgb = mix(color.rgb, half3(1.0), rimOpacity);
    return color;
}
"""
