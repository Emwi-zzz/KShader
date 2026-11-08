package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.TypeFamily

object VectorEmitter {

    fun emit(family: TypeFamily, dim: Int, targetPackage: String): String {
        val scalarName = family.ktName
        val vecName = "${scalarName}$dim"
        val skslName = "${family.skslName}$dim"
        val boolVecName = "SBool$dim"
        val ops = family.preset.ops

        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FieldAccess",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.IndexAccess",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.UnaryOperator"
        )

        return buildFile(targetPackage, imports) {
            block("class $vecName internal constructor(exp: Expression) : SType<$vecName>(exp)") {
                line("override val typeName: String get() = Companion.typeName")
                line("override fun copyWithExp(exp: Expression): $vecName = $vecName(exp)")
                line()

                // Member operators
                OpEmitter.emitVectorMembers(this, vecName, scalarName, boolVecName, ops)
                line()

                // Vector indexing
                line("operator fun get(index: SInt): $scalarName = $scalarName(IndexAccess(this.exp, index.exp))")
                line("operator fun get(index: Int): $scalarName = $scalarName(IndexAccess(this.exp, Literal(index.toString())))")
                line()

                // Swizzles
                emitSwizzles(this, family, dim)
                line()

                block("companion object : STypeCompanion<$vecName>()") {
                    line("override val typeName: String = \"$skslName\"")
                    line("override fun createInstance(exp: Expression): $vecName = $vecName(exp)")
                }
            }
            line()

            // Commuted operators (Scalar <op> VecN)
            OpEmitter.emitVectorCommuted(this, vecName, scalarName, ops)
        }
    }

    private fun emitSwizzles(cb: CodeBuilder, family: TypeFamily, dim: Int) {
        val scalarName = family.ktName
        val xyzwChars = listOf('x', 'y', 'z', 'w').take(dim)
        val rgbaChars = listOf('r', 'g', 'b', 'a').take(dim)

        cb.line("// --- Swizzles ---")
        for (charSet in listOf(xyzwChars, rgbaChars)) {
            for (len in 1..4) {
                val retType = if (len == 1) scalarName else "${scalarName}$len"
                val swizzles = generateSwizzles(charSet, len)
                for (s in swizzles) {
                    cb.line("val $s: $retType get() = $retType(FieldAccess(this.exp, \"$s\"))")
                }
            }
        }
    }

    private fun generateSwizzles(chars: List<Char>, length: Int): List<String> {
        if (length == 1) return chars.map { it.toString() }
        val smaller = generateSwizzles(chars, length - 1)
        return smaller.flatMap { prefix -> chars.map { c -> prefix + c } }
    }
}
