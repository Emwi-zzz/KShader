package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.Matrix
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Scalar
import dev.emwi.zzz.kotlin.sksl.typegen.schema.ShaderTypesSchema
import dev.emwi.zzz.kotlin.sksl.typegen.schema.Vector
import java.io.File

object Generator {

    const val DEFAULT_PACKAGE = "dev.emwi.zzz.kotlin.sksl.dsl.core.type"

    fun generateAll(schema: ShaderTypesSchema, outputDir: File, targetPackage: String = DEFAULT_PACKAGE) {
        val packageDir = File(outputDir, targetPackage.replace('.', '/'))
        packageDir.mkdirs()

        // 1. Emit Type Families
        for (family in schema.families) {
            val preset = family.preset

            // Scalar
            if (preset.shapes.any { it is Scalar }) {
                val file = File(packageDir, "${family.ktName}.kt")
                file.writeText(ScalarEmitter.emit(family, targetPackage))
            }

            // Vectors
            for (vecSpec in preset.shapes.filterIsInstance<Vector>()) {
                for (dim in vecSpec.dims) {
                    val file = File(packageDir, "${family.ktName}$dim.kt")
                    file.writeText(VectorEmitter.emit(family, dim, targetPackage))
                }
            }

            // Matrices
            for (matSpec in preset.shapes.filterIsInstance<Matrix>()) {
                for (dim in matSpec.dims) {
                    val file = File(packageDir, "${family.ktName}${dim}x${dim}.kt")
                    file.writeText(MatrixEmitter.emit(family, dim, targetPackage))
                }
            }
        }

        // 2. Emit Opaque Uniform Types
        for (opaque in schema.opaques) {
            val file = File(packageDir, "${opaque.ktName}.kt")
            file.writeText(OpaqueEmitter.emit(opaque, schema, targetPackage))
        }

        // 3. Emit Literals
        val literalsFile = File(packageDir, "Literals.kt")
        literalsFile.writeText(LiteralEmitter.emit(schema, targetPackage))

        // 4. Emit Constructors
        val constructorsFile = File(packageDir, "Constructors.kt")
        constructorsFile.writeText(ConstructorEmitter.emit(schema, targetPackage))

        // 5. Emit Intrinsics
        val intrinsicsFile = File(packageDir, "Intrinsics.kt")
        intrinsicsFile.writeText(IntrinsicEmitter.emit(schema, targetPackage))
    }
}
