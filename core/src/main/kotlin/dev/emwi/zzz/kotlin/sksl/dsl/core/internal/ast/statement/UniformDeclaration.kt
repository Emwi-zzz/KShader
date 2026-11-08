package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

internal data class UniformDeclaration(
    val name: String,
    val type: String
) : Statement