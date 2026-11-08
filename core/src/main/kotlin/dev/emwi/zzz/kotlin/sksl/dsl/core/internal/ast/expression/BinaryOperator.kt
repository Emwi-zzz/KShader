package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

data class BinaryOperator(val left: Expression, val right: Expression, val op: String) : Expression