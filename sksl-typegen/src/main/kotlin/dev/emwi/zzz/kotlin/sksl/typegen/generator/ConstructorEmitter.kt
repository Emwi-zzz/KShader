package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.Matrix
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShaderTypesSchema
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Vector

object ConstructorEmitter {

    fun emit(schema: ShaderTypesSchema, targetPackage: String): String {
        val imports = listOf(
            "dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.FunctionCall"
        )

        return buildFile(targetPackage, imports) {
            for (family in schema.families) {
                val scalarName = family.ktName
                val scalarSksl = family.skslName

                line("// =================================================================")
                line("// Family: $scalarSksl ($scalarName)")
                line("// =================================================================")

                // 1. Scalar cast / constructors
                line("fun $scalarSksl(x: SType<*>): $scalarName = $scalarName(FunctionCall(\"$scalarSksl\", listOf(x.exp)))")
                line("fun $scalarName(x: SType<*>): $scalarName = $scalarSksl(x)")
                line()

                val preset = family.preset
                val hasVectors = preset.shapes.any { it is Vector }
                val hasMatrices = preset.shapes.any { it is Matrix }

                // 2. Vector constructors
                if (hasVectors) {
                    val vectorSpec = preset.shapes.filterIsInstance<Vector>().first()
                    for (dim in vectorSpec.dims) {
                        val vecName = "${scalarName}$dim"
                        val vecSksl = "${scalarSksl}$dim"

                        // Broadcast
                        line("fun $vecSksl(s: $scalarName): $vecName = $vecName(FunctionCall(\"$vecSksl\", listOf(s.exp)))")
                        line("fun $vecName(s: $scalarName): $vecName = $vecSksl(s)")

                        // Copy / Truncation from vectors of equal or larger dimension
                        for (srcDim in dim..vectorSpec.dims.max()) {
                            val srcVecName = "${scalarName}$srcDim"
                            line("fun $vecSksl(v: $srcVecName): $vecName = $vecName(FunctionCall(\"$vecSksl\", listOf(v.exp)))")
                            line("fun $vecName(v: $srcVecName): $vecName = $vecSksl(v)")
                        }

                        // Full components
                        val compChars = listOf('x', 'y', 'z', 'w').take(dim)
                        val fullParams = compChars.joinToString(", ") { "$it: $scalarName" }
                        val fullArgs = compChars.joinToString(", ") { "$it.exp" }
                        line("fun $vecSksl($fullParams): $vecName = $vecName(FunctionCall(\"$vecSksl\", listOf($fullArgs)))")
                        line("fun $vecName($fullParams): $vecName = $vecSksl(${compChars.joinToString(", ")})")

                        // Composite combinations
                        val partitions = findPartitions(dim).filter { p -> p.size > 1 && p.any { it > 1 } }
                        for (part in partitions) {
                            var charIdx = 0
                            val params = mutableListOf<String>()
                            val callArgs = mutableListOf<String>()
                            val forwardArgs = mutableListOf<String>()

                            for (size in part) {
                                val pChars = compChars.subList(charIdx, charIdx + size).joinToString("")
                                charIdx += size
                                val pType = if (size == 1) scalarName else "${scalarName}$size"
                                params.add("$pChars: $pType")
                                callArgs.add("$pChars.exp")
                                forwardArgs.add(pChars)
                            }

                            val paramList = params.joinToString(", ")
                            val argList = callArgs.joinToString(", ")
                            val forwardList = forwardArgs.joinToString(", ")

                            line("fun $vecSksl($paramList): $vecName = $vecName(FunctionCall(\"$vecSksl\", listOf($argList)))")
                            line("fun $vecName($paramList): $vecName = $vecSksl($forwardList)")
                        }
                        line()
                    }
                }

                // 3. Matrix constructors
                if (hasMatrices) {
                    val matrixSpec = preset.shapes.filterIsInstance<Matrix>().first()
                    for (dim in matrixSpec.dims) {
                        val matName = "${scalarName}${dim}x${dim}"
                        val matSksl = "${scalarSksl}${dim}x${dim}"
                        val vecName = "${scalarName}$dim"

                        // Diagonal broadcast
                        line("fun $matSksl(s: $scalarName): $matName = $matName(FunctionCall(\"$matSksl\", listOf(s.exp)))")
                        line("fun $matName(s: $scalarName): $matName = $matSksl(s)")

                        // Copy / Truncation from matrices of equal or larger dimension
                        for (srcDim in dim..matrixSpec.dims.max()) {
                            val srcMatName = "${scalarName}${srcDim}x${srcDim}"
                            line("fun $matSksl(m: $srcMatName): $matName = $matName(FunctionCall(\"$matSksl\", listOf(m.exp)))")
                            line("fun $matName(m: $srcMatName): $matName = $matSksl(m)")
                        }

                        // From column vectors
                        val colParams = (0 until dim).joinToString(", ") { "c$it: $vecName" }
                        val colArgs = (0 until dim).joinToString(", ") { "c$it.exp" }
                        val colForward = (0 until dim).joinToString(", ") { "c$it" }
                        line("fun $matSksl($colParams): $matName = $matName(FunctionCall(\"$matSksl\", listOf($colArgs)))")
                        line("fun $matName($colParams): $matName = $matSksl($colForward)")

                        // From full scalars
                        val scalarParams = mutableListOf<String>()
                        val scalarArgs = mutableListOf<String>()
                        val scalarForward = mutableListOf<String>()
                        for (r in 0 until dim) {
                            for (c in 0 until dim) {
                                val name = "m$r$c"
                                scalarParams.add("$name: $scalarName")
                                scalarArgs.add("$name.exp")
                                scalarForward.add(name)
                            }
                        }
                        line("fun $matSksl(${scalarParams.joinToString(", ")}): $matName = $matName(FunctionCall(\"$matSksl\", listOf(${scalarArgs.joinToString(", ")})))")
                        line("fun $matName(${scalarParams.joinToString(", ")}): $matName = $matSksl(${scalarForward.joinToString(", ")})")
                        line()
                    }
                }
            }
        }
    }

    private fun findPartitions(target: Int, maxPart: Int = target): List<List<Int>> {
        if (target == 0) return listOf(emptyList())
        val result = mutableListOf<List<Int>>()
        for (p in 1..minOf(target, maxPart)) {
            for (rest in findPartitions(target - p, maxPart)) {
                result.add(listOf(p) + rest)
            }
        }
        return result
    }
}
