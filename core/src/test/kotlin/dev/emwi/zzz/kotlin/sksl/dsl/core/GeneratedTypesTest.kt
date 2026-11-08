package dev.emwi.zzz.kotlin.sksl.dsl.core

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FieldAccess
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.IndexAccess
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.fn
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.loop
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.uniform
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.*
import dev.emwi.zzz.kotlin.sksl.dsl.core.visitor.PlainSkslVisitor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GeneratedTypesTest {

    @Test
    fun `literal extensions produce expected AST literals`() {
        val fExp = assertIs<Literal>(1.5.f.exp)
        assertEquals("1.5", fExp.literal)

        val hExp = assertIs<Literal>(2.5.h.exp)
        assertEquals("2.5", hExp.literal)

        val iExp = assertIs<Literal>(42.i.exp)
        assertEquals("42", iExp.literal)

        val sExp = assertIs<Literal>(7.s.exp)
        assertEquals("7", sExp.literal)

        val uExp = assertIs<Literal>(10.u.exp)
        assertEquals("10u", uExp.literal)

        val usExp = assertIs<Literal>(5.us.exp)
        assertEquals("5u", usExp.literal)

        val bTrueExp = assertIs<Literal>(true.b.exp)
        assertEquals("true", bTrueExp.literal)

        val bFalseExp = assertIs<Literal>(false.b.exp)
        assertEquals("false", bFalseExp.literal)
    }

    @Test
    fun `vector operators produce symmetric and asymmetric binary expressions`() {
        val a = float2(1.0.f, 2.0.f)
        val b = float2(3.0.f, 4.0.f)
        val s = 5.0.f

        // Vec + Vec
        val vAdd = a + b
        val vAddExp = assertIs<BinaryOperator>(vAdd.exp)
        assertEquals("+", vAddExp.op)

        // Vec * Scalar
        val vMulScalar = a * s
        val vMulExp = assertIs<BinaryOperator>(vMulScalar.exp)
        assertEquals("*", vMulExp.op)

        // Scalar * Vec (commuted)
        val sMulVec = s * a
        val sMulExp = assertIs<BinaryOperator>(sMulVec.exp)
        assertEquals("*", sMulExp.op)
    }

    @Test
    fun `matrix multiplication generates valid operations`() {
        val m = float2x2(1.0.f)
        val v = float2(2.0.f, 3.0.f)
        val s = 4.0.f

        // Mat * Scalar
        val mTimesS = m * s
        val mTimesSExp = assertIs<BinaryOperator>(mTimesS.exp)
        assertEquals("*", mTimesSExp.op)

        // Scalar * Mat
        val sTimesM = s * m
        val sTimesMExp = assertIs<BinaryOperator>(sTimesM.exp)
        assertEquals("*", sTimesMExp.op)

        // Mat * Vec
        val mTimesV = m * v
        val mTimesVExp = assertIs<BinaryOperator>(mTimesV.exp)
        assertEquals("*", mTimesVExp.op)

        // Vec * Mat
        val vTimesM = v * m
        val vTimesMExp = assertIs<BinaryOperator>(vTimesM.exp)
        assertEquals("*", vTimesMExp.op)

        // Mat * Mat
        val mTimesM = m * m
        val mTimesMExp = assertIs<BinaryOperator>(mTimesM.exp)
        assertEquals("*", mTimesMExp.op)
    }

    @Test
    fun `vector swizzles generate FieldAccess AST nodes`() {
        val v = float4(1.0.f, 2.0.f, 3.0.f, 4.0.f)

        val xExp = assertIs<FieldAccess>(v.x.exp)
        assertEquals("x", xExp.field)

        val xyExp = assertIs<FieldAccess>(v.xy.exp)
        assertEquals("xy", xyExp.field)

        val xyzExp = assertIs<FieldAccess>(v.xyz.exp)
        assertEquals("xyz", xyzExp.field)

        val wzyxExp = assertIs<FieldAccess>(v.wzyx.exp)
        assertEquals("wzyx", wzyxExp.field)

        val rgbaExp = assertIs<FieldAccess>(v.rgba.exp)
        assertEquals("rgba", rgbaExp.field)
    }

    @Test
    fun `vector and matrix indexing produce IndexAccess nodes`() {
        val v = float3(1.0.f, 2.0.f, 3.0.f)
        val elemExp = assertIs<IndexAccess>(v[0].exp)
        assertEquals("0", assertIs<Literal>(elemExp.index).literal)

        val m = float3x3(1.0.f)
        val colExp = assertIs<IndexAccess>(m[1].exp)
        assertEquals("1", assertIs<Literal>(colExp.index).literal)
    }

    @Test
    fun `composite vector constructors assemble correct expressions`() {
        val xy = float2(1.0.f, 2.0.f)
        val zw = float2(3.0.f, 4.0.f)
        val v4 = float4(xy, zw)

        val call = assertIs<FunctionCall>(v4.exp)
        assertEquals("float4", call.identifier)
        assertEquals(2, call.expressions.size)
    }

    @Test
    fun `intrinsics emit correct SkSL function calls`() {
        val a = float3(1.0.f, 0.0.f, 0.0.f)
        val b = float3(0.0.f, 1.0.f, 0.0.f)

        val c = cross(a, b)
        val cCall = assertIs<FunctionCall>(c.exp)
        assertEquals("cross", cCall.identifier)

        val d = dot(a, b)
        val dCall = assertIs<FunctionCall>(d.exp)
        assertEquals("dot", dCall.identifier)

        val n = normalize(a)
        val nCall = assertIs<FunctionCall>(n.exp)
        assertEquals("normalize", nCall.identifier)

        val cl = clamp(a, 0.0.f, 1.0.f)
        val clCall = assertIs<FunctionCall>(cl.exp)
        assertEquals("clamp", clCall.identifier)

        val m = float2x2(1.0.f)
        val det = determinant(m)
        val detCall = assertIs<FunctionCall>(det.exp)
        assertEquals("determinant", detCall.identifier)
    }

    @Test
    fun `opaque uniforms and eval emit valid SkSL code`() {
        val s = shader {
            val image by uniform(SShader)
            val cf by uniform(SColorFilter)
            val blend by uniform(SBlender)

            main { fragCoord ->
                val color by image.eval(fragCoord)
                val filtered by cf.eval(color)
                val blended by blend.eval(color, filtered)
                blended
            }
        }

        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            uniform shader image;
            uniform colorFilter cf;
            uniform blender blend;
            half4 main(float2 fragCoord) {
                half4 color = image.eval(fragCoord);
                half4 filtered = cf.eval(color);
                half4 blended = blend.eval(color, filtered);
                return blended;
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `unary operators and comparisons emit valid SkSL code`() {
        val s = shader {
            main { fragCoord ->
                val negCoord by -fragCoord
                val b1 by (1.0.f lt 2.0.f)
                val notB by !b1
                val col by half4(negCoord.x.h, negCoord.y.h, 0.0.h, 1.0.h)
                col
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                float2 negCoord = -fragCoord;
                bool b1 = 1.0 < 2.0;
                bool notB = !b1;
                half4 col = half4(half(negCoord.x), half(negCoord.y), 0.0, 1.0);
                return col;
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `swizzles and matrix operations emit valid SkSL code`() {
        val s = shader {
            main { fragCoord ->
                val v by float4(fragCoord.xy, 1.0.f, 0.0.f)
                val sw by v.wzyx
                val m by float4x4(1.0.f)
                val transformed by m * sw
                val firstElem by transformed[0]
                val col by half4(firstElem.h, transformed[1].h, transformed[2].h, transformed[3].h)
                col
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                float4 v = float4(fragCoord.xy, 1.0, 0.0);
                float4 sw = v.wzyx;
                float4x4 m = float4x4(1.0);
                float4 transformed = m * sw;
                float firstElem = transformed[0];
                half4 col = half4(half(firstElem), half(transformed[1]), half(transformed[2]), half(transformed[3]));
                return col;
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `bitwise and integer operations emit valid SkSL code`() {
        val s = shader {
            main { fragCoord ->
                val i1 by 1.i
                val i2 by 2.i
                val andVal by (i1 and i2)
                val orVal by (i1 or i2)
                val xorVal by (i1 xor i2)
                val shlVal by (i1 shl 2)
                val invVal by i1.inv()
                val col by half4(andVal.h, orVal.h, xorVal.h, 1.0.h)
                col
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                int i1 = 1;
                int i2 = 2;
                int andVal = i1 & i2;
                int orVal = i1 | i2;
                int xorVal = i1 ^ i2;
                int shlVal = i1 << 2;
                int invVal = ~i1;
                half4 col = half4(half(andVal), half(orVal), half(xorVal), 1.0);
                return col;
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `notargs raymarching tunnel shader DSL emits valid SkSL`() {
        val notargsShader = shader {
            val iTime by uniform(SFloat)
            val iResolution by uniform(SFloat2)

            val f by fn(SFloat3, SFloat) { p ->
                val p_z by p.z - iTime * 10.0.f
                val a by p_z * 0.1.f
                val p_xy by p.xy * float2x2(cos(a), sin(a), -sin(a), cos(a))
                0.1.f - length(cos(p_xy) + sin(float2(p.y, p_z)))
            }

            main { fragcoord ->
                val d by 0.5.f - float3(fragcoord.xy, 1.0.f) / iResolution.y
                var p by float3(0.0.f)
                loop(0 until 32) { _ ->
                    p += f(p) * d
                }
                val col by (sin(p) + float3(2.0.f, 5.0.f, 12.0.f)) / length(p)
                half4(col.x.h, col.y.h, col.z.h, 1.0.h)
            }
        }

        val sksl = notargsShader.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            uniform float iTime;
            uniform float2 iResolution;
            float f(float3 _p1) {
                float p_z = _p1.z - iTime * 10.0;
                float a = p_z * 0.1;
                float2 p_xy = _p1.xy * float2x2(cos(a), sin(a), -sin(a), cos(a));
                return 0.1 - length(cos(p_xy) + sin(float2(_p1.y, p_z)));
            }
            half4 main(float2 fragCoord) {
                float3 d = 0.5 - float3(fragCoord.xy, 1.0) / iResolution.y;
                float3 p = float3(0.0);
                for (int i = 0; i < 32; i++) {
                    p = p + f(p) * d;
                }
                float3 col = (sin(p) + float3(2.0, 5.0, 12.0)) / length(p);
                return half4(half(col.x), half(col.y), half(col.z), 1.0);
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `half4 combinations with half2, half3, and half`() {
        val s = shader {
            main { fragCoord ->
                val a by half2(1.0.h, 2.0.h)
                val h4_1 by half4(a.xxy, 1.h)
                val h4_2 by half4(a, a)
                val h4_3 by half4(a, 1.h, 2.h)
                val h4_4 by half4(1.h, a, 2.h)
                val h4_5 by half4(1.h, 2.h, a)
                val h4_6 by half4(1.h, a.xxx)
                h4_1
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                half2 a = half2(1.0, 2.0);
                half4 h4_1 = half4(a.xxy, 1);
                half4 h4_2 = half4(a, a);
                half4 h4_3 = half4(a, 1, 2);
                half4 h4_4 = half4(1, a, 2);
                half4 h4_5 = half4(1, 2, a);
                half4 h4_6 = half4(1, a.xxx);
                return h4_1;
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `vector copy and truncation constructors emit valid SkSL`() {
        val s = shader {
            main { fragCoord ->
                val v4 by float4(fragCoord.xy, 1.0.f, 2.0.f)
                val v3 by float3(v4)
                val v2 by float2(v4)
                val v2_from3 by float2(v3)
                val v4_copy by float4(v4)
                half4(v4_copy.x.h, v3.y.h, v2.x.h, v2_from3.y.h)
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                float4 v4 = float4(fragCoord.xy, 1.0, 2.0);
                float3 v3 = float3(v4);
                float2 v2 = float2(v4);
                float2 v2_from3 = float2(v3);
                float4 v4_copy = float4(v4);
                return half4(half(v4_copy.x), half(v3.y), half(v2.x), half(v2_from3.y));
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `vector and matrix whole-object precision conversions emit valid SkSL`() {
        val s = shader {
            main { fragCoord ->
                val h4 by half4(1.0.h, 2.0.h, 3.0.h, 4.0.h)
                val f4 by h4.f
                val h4_back by f4.h
                val h2 by (fragCoord).h
                val f2 by h2.f
                val f2x2 by float2x2(1.0.f)
                val h2x2 by f2x2.h
                val f2x2_back by h2x2.f
                half4(h4_back.xyz, 1.0.h)
            }
        }
        val sksl = s.toSkslString(PlainSkslVisitor())
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                half4 h4 = half4(1.0, 2.0, 3.0, 4.0);
                float4 f4 = float4(h4);
                half4 h4_back = half4(f4);
                half2 h2 = half2(fragCoord);
                float2 f2 = float2(h2);
                float2x2 f2x2 = float2x2(1.0);
                half2x2 h2x2 = half2x2(f2x2);
                float2x2 f2x2_back = float2x2(h2x2);
                return half4(h4_back.xyz, 1.0);
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }

    @Test
    fun `nested loops generate non-conflicting loop variables`() {
        val s = shader {
            main { _ ->
                var sum by 0.0.f
                loop(0 until 4) { _ ->
                    loop(0 until 4) { _ ->
                        sum = sum + 1.0.f
                    }
                }
                half4(sum.h, 0.0.h, 0.0.h, 1.0.h)
            }
        }
        val sksl = s.toSkslString()
        assertEquals(
            """
            half4 main(float2 fragCoord) {
                float sum = 0.0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        sum = sum + 1.0;
                    }
                }
                return half4(half(sum), 0.0, 0.0, 1.0);
            }
            """.trimIndent(),
            sksl.trimIndent()
        )
    }
}
