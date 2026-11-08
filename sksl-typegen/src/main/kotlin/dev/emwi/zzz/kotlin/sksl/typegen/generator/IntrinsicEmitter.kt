package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.IntrinsicDef
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Matrix
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Scalar
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShaderTypesSchema
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShapeFilter
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShapeSpec
import dev.emwi.zzz.kotlin.sksl.typegen.schema.TypeFamily
import dev.emwi.zzz.kotlin.sksl.typegen.schema.TypeRef
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Vector

object IntrinsicEmitter {

    private sealed interface CurrentShape {
        object ScalarShape : CurrentShape
        data class VectorShape(val dim: Int) : CurrentShape
        data class MatrixShape(val dim: Int) : CurrentShape
    }

    fun emit(schema: ShaderTypesSchema, targetPackage: String): String {
        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall"
        )

        val emittedSignatures = mutableSetOf<String>()

        return buildFile(targetPackage, imports) {
            for (family in schema.families) {
                line("// =================================================================")
                line("// Intrinsics for Family: ${family.skslName} (${family.ktName})")
                line("// =================================================================")

                val shapes = mutableListOf<CurrentShape>()
                for (spec in family.preset.shapes) {
                    when (spec) {
                        is Scalar -> shapes.add(CurrentShape.ScalarShape)
                        is Vector -> spec.dims.forEach { shapes.add(CurrentShape.VectorShape(it)) }
                        is Matrix -> spec.dims.forEach { shapes.add(CurrentShape.MatrixShape(it)) }
                    }
                }

                for (bundle in family.preset.intrinsics) {
                    line("// --- Bundle: ${bundle.name} ---")
                    for (def in bundle.definitions) {
                        for (shape in shapes) {
                            if (!matchesFilter(def.filter, shape)) continue

                            val returnKtType = resolveTypeRef(def.returnType, family, shape)
                            val paramKtTypes = def.params.map { resolveTypeRef(it, family, shape) }

                            val sigKey = "${def.name}(${paramKtTypes.joinToString(",")})"
                            if (!emittedSignatures.add(sigKey)) continue

                            val paramNames = generateParamNames(def.name, paramKtTypes)
                            val paramDecls = paramNames.zip(paramKtTypes).joinToString(", ") { "${it.first}: ${it.second}" }
                            val argExps = paramNames.joinToString(", ") { "$it.exp" }

                            line("fun ${def.name}($paramDecls): $returnKtType = $returnKtType(FunctionCall(\"${def.skslName}\", listOf($argExps)))")
                        }
                    }
                    line()
                }

                // Vector comparison top-level functions (lessThan, equal, etc.)
                for (spec in family.preset.shapes.filterIsInstance<Vector>()) {
                    for (dim in spec.dims) {
                        val vecName = "${family.ktName}$dim"
                        val boolVecName = "SBool$dim"

                        val compFuncs = mutableListOf<String>()
                        if (dev.emwi.zzz.kotlin.sksl.typegen.schema.Op.Compare in family.preset.ops) {
                            compFuncs.addAll(listOf("lessThan", "lessThanEqual", "greaterThan", "greaterThanEqual"))
                        }
                        if (dev.emwi.zzz.kotlin.sksl.typegen.schema.Op.Equal in family.preset.ops) {
                            compFuncs.addAll(listOf("equal", "notEqual"))
                        }
                        for (cf in compFuncs) {
                            val sigKey = "$cf($vecName,$vecName)"
                            if (emittedSignatures.add(sigKey)) {
                                line("fun $cf(x: $vecName, y: $vecName): $boolVecName = x.$cf(y)")
                            }
                        }
                    }
                }
                line()
            }
        }
    }

    private fun matchesFilter(filter: ShapeFilter, shape: CurrentShape): Boolean = when (filter) {
        ShapeFilter.All -> true
        ShapeFilter.ScalarOnly -> shape is CurrentShape.ScalarShape
        ShapeFilter.VectorOnly -> shape is CurrentShape.VectorShape
        ShapeFilter.MatrixOnly -> shape is CurrentShape.MatrixShape
        ShapeFilter.VecAndScalar -> shape is CurrentShape.ScalarShape || shape is CurrentShape.VectorShape
        ShapeFilter.Dim3VectorOnly -> shape is CurrentShape.VectorShape && shape.dim == 3
    }

    private fun resolveTypeRef(ref: TypeRef, family: TypeFamily, shape: CurrentShape): String = when (ref) {
        is TypeRef.Self -> when (shape) {
            is CurrentShape.ScalarShape -> family.ktName
            is CurrentShape.VectorShape -> "${family.ktName}${shape.dim}"
            is CurrentShape.MatrixShape -> "${family.ktName}${shape.dim}x${shape.dim}"
        }
        is Scalar -> family.ktName
        is TypeRef.SameDimBool -> when (shape) {
            is CurrentShape.ScalarShape -> "SBool"
            is CurrentShape.VectorShape -> "SBool${shape.dim}"
            is CurrentShape.MatrixShape -> "SBool"
        }
        is TypeRef.BoolScalar -> "SBool"
        is TypeRef.Mat -> when (shape) {
            is CurrentShape.MatrixShape -> "${family.ktName}${shape.dim}x${shape.dim}"
            is CurrentShape.VectorShape -> "${family.ktName}${shape.dim}x${shape.dim}"
            is CurrentShape.ScalarShape -> "${family.ktName}2x2"
        }
        is TypeRef.Vec -> {
            val d = ref.dim ?: when (shape) {
                is CurrentShape.VectorShape -> shape.dim
                is CurrentShape.MatrixShape -> shape.dim
                is CurrentShape.ScalarShape -> 2
            }
            "${family.ktName}$d"
        }
        is TypeRef.Explicit -> ref.name
    }

    private fun generateParamNames(funcName: String, types: List<String>): List<String> = when (types.size) {
        1 -> listOf("x")
        2 -> when (funcName) {
            "step" -> listOf("edge", "x")
            "distance" -> listOf("p0", "p1")
            "reflect" -> listOf("i", "n")
            else -> listOf("x", "y")
        }
        3 -> when (funcName) {
            "clamp" -> listOf("x", "minVal", "maxVal")
            "mix" -> listOf("x", "y", "a")
            "smoothstep" -> listOf("edge0", "edge1", "x")
            "faceforward" -> listOf("n", "i", "nref")
            "refract" -> listOf("eta", "n", "etaRatio")
            else -> listOf("x", "y", "z")
        }
        else -> types.indices.map { "p$it" }
    }
}
