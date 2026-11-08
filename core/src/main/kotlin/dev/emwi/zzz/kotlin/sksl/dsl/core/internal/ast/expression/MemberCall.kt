package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression

internal data class MemberCall(
    val receiver: Expression,
    val methodName: String,
    val arguments: List<Expression>
) : Expression
