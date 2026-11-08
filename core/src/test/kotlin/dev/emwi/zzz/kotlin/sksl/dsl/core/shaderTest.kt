package dev.emwi.zzz.kotlin.sksl.dsl.core

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.FunctionDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.If
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Parameter
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Return
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.UniformDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.sIf
import dev.emwi.zzz.kotlin.sksl.dsl.core.sema.uniform
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SBool
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SFloat2
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SHalf4
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.sh
import kotlin.test.Test
import kotlin.test.assertEquals

class ShaderTest {
    @Test
    fun `valid AST generation test`(): Unit {

        val expectedTree = Block(
UniformDeclaration(
                "u_resolution",
                SFloat2.typeName
            ),
            FunctionDeclaration(
                "main",
                SHalf4.typeName,
                listOf(
                    Parameter(
                        "fragCoord",
                        SFloat2.typeName
                    )
                ),
                Block(
VarDeclaration(
                        "half",
                        "y",
                        Literal("1.0")
                    ),
                    If(
                        Literal("true"),
                        Block(
VarAssignment(
                                "y",
                                Literal("2.0")
                            )
                        )
                    ),
                    VarDeclaration(
                        "half4",
                        "x",
                        FunctionCall(
                            "half4",
                            listOf(
                                FunctionCall(
                                    "half2",
                                    listOf(
                                        BinaryOperator(
                                            Identifier("fragCoord"),
                                            Identifier("u_resolution"),
                                            "/",
                                        )
                                    )
                                ),
                                Literal("1.0"),
                                Literal("0.0"),
                            )
                        )
                    ),
                    Return(Identifier("x"))
                )
            ),
        )

        val shader = shader {
            val u_resolution by uniform(SFloat2)
            main { fragCoord ->
                var y by 1.0.sh
                sIf(SBool(Literal("true"))) {
                    y = 2.0.sh
                }

                val x by SHalf4((fragCoord / u_resolution).sh, 1.0.sh, 0.0.sh)
                x
            }
        }

        assertEquals(
            expected = expectedTree,
            actual = shader.scope.block,
            message = "Shader AST should match the expected structure"
        )
    }

    @Test
    fun `explicit name uniform declaration matches delegated uniform`(): Unit {
        val delegatedShader = shader {
            val u_resolution by uniform(SFloat2)
            main { fragCoord ->
                val x by SHalf4((fragCoord / u_resolution).sh, 1.0.sh, 0.0.sh)
                x
            }
        }

        val explicitNamedShader = shader {
            val u_resolution = uniform(SFloat2, "u_resolution")
            main { fragCoord ->
                val x by SHalf4((fragCoord / u_resolution).sh, 1.0.sh, 0.0.sh)
                x
            }
        }

        assertEquals(
            expected = explicitNamedShader.scope.block,
            actual = delegatedShader.scope.block,
            message = "Delegated and explicitly named uniform declarations should yield identical ASTs"
        )
    }
}