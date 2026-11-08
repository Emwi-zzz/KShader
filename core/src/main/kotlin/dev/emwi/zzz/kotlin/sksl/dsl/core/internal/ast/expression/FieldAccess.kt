package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

data class FieldAccess(val expression: Expression, val field: String) :
    Expression
