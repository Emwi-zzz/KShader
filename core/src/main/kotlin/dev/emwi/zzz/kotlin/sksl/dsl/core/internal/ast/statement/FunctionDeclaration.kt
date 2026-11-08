package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block

internal data class Parameter(
    val name: String,
    val type: String
)

internal data class FunctionDeclaration(
    val name: String,
    val returnType: String,
    val parameters: List<Parameter>,
    val body: Block
) : Statement