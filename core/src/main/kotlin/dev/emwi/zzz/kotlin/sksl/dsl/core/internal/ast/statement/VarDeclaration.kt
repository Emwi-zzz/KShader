package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression

internal data class VarDeclaration(
    val type: String,
    val identifier: String,
    val initializer: Expression
) : Statement