package dev.emwi.zzz.kotlin.sksl.dsl.core.visitor

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.*
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.*

abstract class ShaderAstVisitor<T> {
    internal fun execute(node: Statement): Unit = when (node) {
        is FunctionDeclaration -> visitFunctionDeclaration(node)
        is Return -> visitReturn(node)
        is UniformDeclaration -> visitUniformDeclaration(node)
        is VarAssignment -> visitVarAssignment(node)
        is VarDeclaration -> visitVarDeclaration(node)
        is If -> visitIf(node)
        is For -> visitFor(node)
        is While -> visitWhile(node)
        is ExpressionStatement -> visitExpressionStatement(node)
    }

    internal fun evaluate(expression: Expression): T = when (expression) {
        is FunctionCall -> visitFunctionCall(expression)
        is MemberCall -> visitMemberCall(expression)
        is Identifier -> visitIdentifier(expression)
        is Literal -> visitLiteral(expression)
        is BinaryOperator -> visitBinaryOperator(expression)
        is UnaryOperator -> visitUnaryOperator(expression)
        is FieldAccess -> visitFieldAccess(expression)
        is IndexAccess -> visitIndexAccess(expression)
    }

    internal fun visitBlock(block: Block) {
        block.statements.forEach { execute(it) }
    }

    internal abstract fun visitFunctionDeclaration(node: FunctionDeclaration)
    internal abstract fun visitReturn(node: Return)
    internal abstract fun visitUniformDeclaration(node: UniformDeclaration)
    internal abstract fun visitVarAssignment(node: VarAssignment)
    internal abstract fun visitIf(node: If)
    internal abstract fun visitFor(node: For)
    internal abstract fun visitWhile(node: While)
    internal abstract fun visitExpressionStatement(node: ExpressionStatement)
    internal abstract fun visitVarDeclaration(node: VarDeclaration)

    internal abstract fun visitFunctionCall(node: FunctionCall): T
    internal abstract fun visitMemberCall(node: MemberCall): T
    internal abstract fun visitIdentifier(node: Identifier): T
    internal abstract fun visitLiteral(node: Literal): T
    internal abstract fun visitBinaryOperator(node: BinaryOperator): T
    internal abstract fun visitUnaryOperator(node: UnaryOperator): T
    internal abstract fun visitFieldAccess(node: FieldAccess): T
    internal abstract fun visitIndexAccess(node: IndexAccess): T

    internal abstract fun getResult(): T
}
