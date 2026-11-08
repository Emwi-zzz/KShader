package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.Matrix
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShaderTypesSchema
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Vector

object LiteralEmitter {

    fun emit(schema: ShaderTypesSchema, targetPackage: String): String {
        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall",
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal"
        )

        return buildFile(targetPackage, imports) {
            line("// --- Primitive Literal Extensions ---")
            for (family in schema.families) {
                val ktName = family.ktName
                val ext = family.ext

                when (family.primitive) {
                    Float::class -> {
                        line("val Number.$ext: $ktName get() = $ktName(Literal(this.toString()))")
                        line("val Number.s$ext: $ktName get() = $ext")
                    }
                    Int::class, Short::class -> {
                        line("val Number.$ext: $ktName get() = $ktName(Literal(this.toLong().toString()))")
                        line("val Number.s$ext: $ktName get() = $ext")
                    }
                    UInt::class, UShort::class -> {
                        line("val Number.$ext: $ktName get() = $ktName(Literal(\"\${this.toLong()}u\"))")
                        line("val UInt.$ext: $ktName get() = $ktName(Literal(\"\${this}u\"))")
                        line("val Number.s$ext: $ktName get() = $ext")
                        line("val UInt.s$ext: $ktName get() = $ext")
                    }
                    Boolean::class -> {
                        line("val Boolean.$ext: $ktName get() = $ktName(Literal(this.toString()))")
                        line("val Boolean.s$ext: $ktName get() = $ext")
                    }
                }
            }
            line()

            line("// --- Precision Conversion Extensions for Scalars, Vectors, and Matrices ---")
            for (targetFamily in schema.families) {
                val targetExt = targetFamily.ext
                val targetScalarName = targetFamily.ktName
                val targetSkslName = targetFamily.skslName
                val targetPreset = targetFamily.preset

                // 1. Scalar precision conversions
                for (srcFamily in schema.families) {
                    val srcScalarName = srcFamily.ktName
                    if (srcFamily == targetFamily) {
                        line("val $srcScalarName.$targetExt: $targetScalarName get() = this")
                        line("val $srcScalarName.s$targetExt: $targetScalarName get() = this")
                    } else {
                        line("val $srcScalarName.$targetExt: $targetScalarName get() = $targetScalarName(FunctionCall(\"$targetSkslName\", listOf(this.exp)))")
                        line("val $srcScalarName.s$targetExt: $targetScalarName get() = $targetScalarName(FunctionCall(\"$targetSkslName\", listOf(this.exp)))")
                    }
                }

                // 2. Vector precision conversions
                val targetHasVectors = targetPreset.shapes.any { it is Vector }
                if (targetHasVectors) {
                    val targetVectorSpec = targetPreset.shapes.filterIsInstance<Vector>().first()
                    for (dim in targetVectorSpec.dims) {
                        val targetVecName = "${targetScalarName}$dim"
                        val targetVecSksl = "${targetSkslName}$dim"
                        val isShadowedBySwizzle = (targetExt == "b" && dim >= 3)

                        for (srcFamily in schema.families) {
                            val srcHasVectors = srcFamily.preset.shapes.any { it is Vector }
                            if (srcHasVectors) {
                                val srcVectorSpec = srcFamily.preset.shapes.filterIsInstance<Vector>().first()
                                if (dim in srcVectorSpec.dims) {
                                    val srcVecName = "${srcFamily.ktName}$dim"
                                    if (srcFamily == targetFamily) {
                                        if (!isShadowedBySwizzle) {
                                            line("val $srcVecName.$targetExt: $targetVecName get() = this")
                                        }
                                        line("val $srcVecName.s$targetExt: $targetVecName get() = this")
                                    } else {
                                        if (!isShadowedBySwizzle) {
                                            line("val $srcVecName.$targetExt: $targetVecName get() = $targetVecName(FunctionCall(\"$targetVecSksl\", listOf(this.exp)))")
                                        }
                                        line("val $srcVecName.s$targetExt: $targetVecName get() = $targetVecName(FunctionCall(\"$targetVecSksl\", listOf(this.exp)))")
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Matrix precision conversions
                val targetHasMatrices = targetPreset.shapes.any { it is Matrix }
                if (targetHasMatrices) {
                    val targetMatrixSpec = targetPreset.shapes.filterIsInstance<Matrix>().first()
                    for (dim in targetMatrixSpec.dims) {
                        val targetMatName = "${targetScalarName}${dim}x${dim}"
                        val targetMatSksl = "${targetSkslName}${dim}x${dim}"

                        for (srcFamily in schema.families) {
                            val srcHasMatrices = srcFamily.preset.shapes.any { it is Matrix }
                            if (srcHasMatrices) {
                                val srcMatrixSpec = srcFamily.preset.shapes.filterIsInstance<Matrix>().first()
                                if (dim in srcMatrixSpec.dims) {
                                    val srcMatName = "${srcFamily.ktName}${dim}x${dim}"
                                    if (srcFamily == targetFamily) {
                                        line("val $srcMatName.$targetExt: $targetMatName get() = this")
                                        line("val $srcMatName.s$targetExt: $targetMatName get() = this")
                                    } else {
                                        line("val $srcMatName.$targetExt: $targetMatName get() = $targetMatName(FunctionCall(\"$targetMatSksl\", listOf(this.exp)))")
                                        line("val $srcMatName.s$targetExt: $targetMatName get() = $targetMatName(FunctionCall(\"$targetMatSksl\", listOf(this.exp)))")
                                    }
                                }
                            }
                        }
                    }
                }
                line()
            }
        }
    }
}
