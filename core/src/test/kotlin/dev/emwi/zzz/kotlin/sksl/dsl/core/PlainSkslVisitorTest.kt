package dev.emwi.zzz.kotlin.sksl.dsl.core

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.fn
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.sIf
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.uniform
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SBool
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SFloat2
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SHalf
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SHalf4
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.sh
import kotlin.test.Test
import kotlin.test.assertEquals

class PlainSkslVisitorTest {
    @Test
    fun test() {
        val shader = shader {
            val u_resolution by uniform(SFloat2)

            main { fragCoord ->
                var y by 1.0.sh
                sIf(SBool(Literal("true"))) {
                    y = 2.0.sh
                } otherwise {
                    y = 3.0.sh
                }

                val x by SHalf4((fragCoord / u_resolution).sh, 1.0.sh, 0.0.sh)
                x
            }
        }
        assertEquals(
            """
            uniform float2 u_resolution;
            half4 main(float2 fragCoord) {
                half y = 1.0;
                if(true) {
                    y = 2.0;
                } else {
                    y = 3.0;
                }
                half4 x = half4(half2(fragCoord / u_resolution), 1.0, 0.0);
                return x;
            }
            """.trimIndent(),
            shader.toSkslString().trimIndent()
        )
    }

}