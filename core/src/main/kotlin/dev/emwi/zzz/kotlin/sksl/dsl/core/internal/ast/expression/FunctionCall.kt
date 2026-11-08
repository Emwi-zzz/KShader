package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

data class FunctionCall(val identifier: String, val expressions: List<Expression>) :
    Expression {
    constructor(identifier: String, vararg expressions: Expression) : this(identifier, expressions.toList())
}