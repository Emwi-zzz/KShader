package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

data class UnaryOperator(val expression: Expression, val op: String, val prefix: Boolean = true) :
    Expression
