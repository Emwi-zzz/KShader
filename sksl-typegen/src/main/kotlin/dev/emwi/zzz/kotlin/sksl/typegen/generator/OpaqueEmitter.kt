package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.OpaqueType
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShaderTypesSchema

object OpaqueEmitter {

    fun emit(opaque: OpaqueType, schema: ShaderTypesSchema, targetPackage: String): String {
        val ktName = opaque.ktName
        val skslName = opaque.skslName

        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.MemberCall"
        )

        return buildFile(targetPackage, imports) {
            block("class $ktName internal constructor(exp: Expression) : SType<$ktName>(exp)") {
                line("override val typeName: String get() = Companion.typeName")
                line("override fun copyWithExp(exp: Expression): $ktName = $ktName(exp)")
                line()

                for (method in opaque.methods) {
                    val returnKtType = resolveSkslTypeToKt(method.returnType, schema)
                    val paramList = method.params.joinToString(", ") { "${it.name}: ${resolveSkslTypeToKt(it.type, schema)}" }
                    val argExps = method.params.joinToString(", ") { "${it.name}.exp" }

                    block("fun ${method.name}($paramList): $returnKtType") {
                        line("return $returnKtType(MemberCall(this.exp, \"${method.name}\", listOf($argExps)))")
                    }
                }
                line()

                block("companion object : STypeCompanion<$ktName>()") {
                    line("override val typeName: String = \"$skslName\"")
                    line("override fun createInstance(exp: Expression): $ktName = $ktName(exp)")
                }
            }
        }
    }

    fun resolveSkslTypeToKt(skslType: String, schema: ShaderTypesSchema): String {
        for (f in schema.families) {
            if (skslType == f.skslName) return f.ktName
            for (dim in 2..4) {
                if (skslType == "${f.skslName}$dim") return "${f.ktName}$dim"
                if (skslType == "${f.skslName}${dim}x${dim}") return "${f.ktName}${dim}x${dim}"
            }
        }
        for (o in schema.opaques) {
            if (skslType == o.skslName) return o.ktName
        }
        return skslType
    }
}
