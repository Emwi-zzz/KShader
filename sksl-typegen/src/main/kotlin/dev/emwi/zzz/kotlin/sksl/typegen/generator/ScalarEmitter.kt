package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.TypeFamily

object ScalarEmitter {
    fun emit(family: TypeFamily, targetPackage: String): String {
        val ktName = family.ktName
        val skslName = family.skslName
        val ops = family.preset.ops

        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.UnaryOperator"
        )

        return buildFile(targetPackage, imports) {
            block("class $ktName internal constructor(exp: Expression) : SType<$ktName>(exp)") {
                line("override val typeName: String get() = Companion.typeName")
                line("override fun copyWithExp(exp: Expression): $ktName = $ktName(exp)")
                line()
                OpEmitter.emitScalarMembers(this, ktName, ops)
                line()
                block("companion object : STypeCompanion<$ktName>()") {
                    line("override val typeName: String = \"$skslName\"")
                    line("override fun createInstance(exp: Expression): $ktName = $ktName(exp)")
                }
            }
        }
    }
}
