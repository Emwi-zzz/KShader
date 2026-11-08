package dev.emwi.zzz.kotlin.sksl.typegen

import java.io.File

object FnGenerator {

    val builder = StringBuilder()

    const val DEFAULT_PACKAGE = "dev.emwi.zzz.kotlin.sksl.dsl.core.sema"

    const val HEADER = """package $DEFAULT_PACKAGE

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.FunctionDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Parameter
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.FunctionScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.processReturnBlock
import dev.emwi.zzz.kotlin.sksl.dsl.core.shader
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SFloat
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SType
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.STypeCompanion
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.f
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty"""

    fun buildUnit(x: Int) {
        val genericParams = (1..x).joinToString(", ") { "T$it : SType<T$it>" }
        val genericArgs = (1..x).joinToString(", ") { "T$it" }
        val parameterTypes = (1..x).joinToString(", ") { "parameterType$it: STypeCompanion<T$it>" }
        val parameterVal = (1..x).joinToString(", ") { "val parameterType$it: STypeCompanion<T$it>" }
        val parameterForwarding = (1..x).joinToString(", ") { "parameterType$it" }
        val lambdaParams = (1..x).joinToString(", ") { "T$it" }

        // Generates: parameterType1.createInstance(Identifier("_p1")), parameterType2.createInstance(Identifier("_p2")), ...
        val bodyArgs = (1..x).joinToString(", ") { "parameterType$it.createInstance(Identifier(\"_p$it\"))" }

        // Generates: Parameter("_p1", parameterType1.typeName), Parameter("_p2", parameterType2.typeName), ...
        val parameterDeclarations = (1..x).joinToString(", ") { "Parameter(\"_p$it\", parameterType$it.typeName)" }

        // Generates: t1: T1, t2: T2, ...
        val invokerParams = (1..x).joinToString(", ") { "t$it: T$it" }

        // Generates: t1.exp, t2.exp, ...
        val callArgs = (1..x).joinToString(", ") { "t$it.exp" }

        // Generates the return type signature for ReadOnlyProperty: (T1, T2) -> R
        val fnSignature = "($lambdaParams) -> R"

        builder.append("\n\n")
        builder.append("""
        class FnDelegate$x<$genericParams, R : SType<R>> internal constructor(
            $parameterVal,
            val returnToken: STypeCompanion<R>,
            val scope: ShaderScope,
            val body: FunctionScope<R>.($lambdaParams) -> R
        ) {
            operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, $fnSignature> {
                with(scope) {
                    val funName = property.name

                    val block = FunctionScope.use<Block, R> {
                        processReturnBlock {
                            body($bodyArgs)
                        }
                        block
                    }

                    scope.addStatement(
                        FunctionDeclaration(
                            funName,
                            returnToken.typeName,
                            listOf($parameterDeclarations),
                            block
                        )
                    )

                    val invoker = { $invokerParams ->
                        returnToken.createInstance(FunctionCall(funName, listOf($callArgs)))
                    }

                    return ReadOnlyProperty { _, _ -> invoker }
                }
            }
        }

        context(scope: ShaderScope)
        fun <$genericParams, R : SType<R>> fn(
            $parameterTypes,
            returnType: STypeCompanion<R>,
            block: FunctionScope<R>.($lambdaParams) -> R
        ): FnDelegate$x<$genericArgs, R> {
            return FnDelegate$x($parameterForwarding, returnType, scope, block)
        }
    """.trimIndent())
    }
    fun build(range: IntRange){
        builder.append(HEADER)
        for (i in range) {
            buildUnit(i)
            builder.append("\n")
        }
    }

    fun writeTo(outputDir: File) {
        val packageDir = File(outputDir, DEFAULT_PACKAGE.replace('.', '/'))
        packageDir.mkdirs()
        val file = File(packageDir, "Fn.kt")
        file.writeText(builder.toString())
    }

    fun generate(range: IntRange = 0..20, outputDir: File){
        build(range)
        writeTo(outputDir)
    }
}