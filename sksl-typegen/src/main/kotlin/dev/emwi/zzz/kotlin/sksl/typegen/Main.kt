package dev.emwi.zzz.kotlin.sksl.typegen

import dev.emwi.zzz.kotlin.sksl.typegen.generator.Generator
import dev.emwi.zzz.kotlin.sksl.typegen.schema.SkslTypes
import java.io.File

fun main(args: Array<String>) {
    val outputDirPath = if (args.isNotEmpty()) {
        args[0]
    } else {
        throw IllegalArgumentException("Output directory path must be provided as an argument.")
    }

    val outputDir = File(outputDirPath)
    println("Generating SkSL types into: ${outputDir.absolutePath}")
    Generator.generateAll(SkslTypes, outputDir)
    println("Generation completed successfully.")
    println("Generating Functions wrappers into: ${outputDir.absolutePath}")
    FnGenerator.generate(1..20, outputDir)
    println("Generation completed successfully.")
}
