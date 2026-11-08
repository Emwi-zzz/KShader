package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

data class IndexAccess(val expression: Expression, val index: Expression) :
    Expression
