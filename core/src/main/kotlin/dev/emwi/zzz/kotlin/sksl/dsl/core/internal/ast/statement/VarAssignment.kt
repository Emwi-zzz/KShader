package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression

internal data class VarAssignment(val identifier: String, val expression: Expression) :
    Statement