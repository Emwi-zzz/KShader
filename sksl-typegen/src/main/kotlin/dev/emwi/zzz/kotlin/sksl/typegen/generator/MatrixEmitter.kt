package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.TypeFamily

object MatrixEmitter {

    fun emit(family: TypeFamily, dim: Int, targetPackage: String): String {
        val scalarName = family.ktName
        val vecName = "${scalarName}$dim"
        val matName = "${scalarName}${dim}x${dim}"
        val skslName = "${family.skslName}${dim}x${dim}"
        val ops = family.preset.ops

        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.IndexAccess",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.UnaryOperator"
        )

        return buildFile(targetPackage, imports) {
            block("class $matName internal constructor(exp: Expression) : SType<$matName>(exp)") {
                line("override val typeName: String get() = Companion.typeName")
                line("override fun copyWithExp(exp: Expression): $matName = $matName(exp)")
                line()

                // Member operators
                OpEmitter.emitMatrixMembers(this, matName, scalarName, vecName, ops)
                line()

                // Matrix column indexing: m[col] -> column vector
                line("operator fun get(col: SInt): $vecName = $vecName(IndexAccess(this.exp, col.exp))")
                line("operator fun get(col: Int): $vecName = $vecName(IndexAccess(this.exp, Literal(col.toString())))")
                line()

                block("companion object : STypeCompanion<$matName>()") {
                    line("override val typeName: String = \"$skslName\"")
                    line("override fun createInstance(exp: Expression): $matName = $matName(exp)")
                }
            }
            line()

            // Commuted operators (Scalar <op> MatN, VecN * MatN)
            OpEmitter.emitMatrixCommuted(this, matName, scalarName, vecName, ops)
        }
    }
}
