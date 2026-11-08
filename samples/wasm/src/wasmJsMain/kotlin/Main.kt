package dev.emwi.zzz.kotlin.sksl.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.window.ComposeViewport
import dev.emwi.zzz.kotlin.sksl.dsl.core.*
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.fn
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.loop
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.uniform
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.*
import kotlinx.browser.document
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/**
 * Builds the @notargs raymarching tunnel shader via the KShader eDSL.
 * Source: https://twitter.com/notargs/status/1250468645030858753
 */

fun buildNotargsShaderCode(): String {
    val notargsShader = shader {
        val iTime by uniform(SFloat)
        val iResolution by uniform(SFloat2)

        val f by fn(SFloat3, SFloat) { p ->
            val p_z by p.z - iTime * 10.0.f
            val a by p_z * 0.1.f
            val p_xy by p.xy * float2x2(cos(a), sin(a), -sin(a), cos(a))
            val qwe by 0.1.f - length(cos(p_xy) + sin(float2(p.y, p_z)))
            returns(qwe)
        }

        main { fragcoord ->
            val d by 0.5.f - float3(fragcoord.xy, 1.0.f) / iResolution.y
            var p by float3(0.0.f)
            loop(0 until 32) {
                p += f(p) * d
            }
            val col by (sin(p) + float3(2.0.f, 5.0.f, 12.0.f)) / length(p)
            half4(col.h, 1.0.h)
        }
    }
    return notargsShader.toSkslString()
}

@Composable
fun TunnelShaderView() {
    val sksl = remember { buildNotargsShaderCode() }
    val runtimeEffect = remember(sksl) { RuntimeEffect.makeForShader(sksl) }

    val timeSeconds by produceState(0f) {
        val startNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { frameTimeNanos ->
                value = (frameTimeNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val builder = RuntimeShaderBuilder(runtimeEffect)
        builder.uniform("iTime", timeSeconds)
        builder.uniform("iResolution", size.width, size.height)
        val shader = builder.makeShader().asComposeShader()
        drawRect(brush = ShaderBrush(shader))
    }
}

@Composable
fun App() {
    MaterialTheme {
        TunnelShaderView()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val container = document.getElementById("compose-container")
    if (container != null) {
        ComposeViewport("compose-container") {
            App()
        }
    } else {
        ComposeViewport {
            App()
        }
    }
}
