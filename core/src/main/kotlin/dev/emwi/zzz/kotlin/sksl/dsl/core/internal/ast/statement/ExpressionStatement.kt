package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression

internal data class ExpressionStatement(val expression: Expression) :
    Statement
