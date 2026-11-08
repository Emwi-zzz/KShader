package dev.emwi.zzz.kotlin.sksl.dsl.core.visitor

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.*
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.*
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.writer.CodeWriter

class PlainSkslVisitor : ShaderAstVisitor<String>() {
    private val writer = CodeWriter()

    override fun visitFunctionDeclaration(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.FunctionDeclaration) {
        writer.writeBlock("${node.returnType} ${node.name}(${node.parameters.joinToString(", ") { visitParam(it) }})") {
            node.body.statements.forEach { execute(it) }
        }
    }

    private fun visitParam(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Parameter): String {
        return "${node.type} ${node.name}"
    }

    override fun visitReturn(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Return) {
        val exp = node.expression?.let { " ${evaluate(it)}" } ?: ""
        writer.writeln("return${exp};")
    }

    override fun visitUniformDeclaration(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.UniformDeclaration) {
        writer.writeln("uniform ${node.type} ${node.name};")
    }

    override fun visitVarAssignment(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment) {
        writer.writeln("${node.identifier} = ${evaluate(node.expression)};")
    }

    override fun visitVarDeclaration(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarDeclaration) {
        writer.writeln("${node.type} ${node.identifier} = ${evaluate(node.initializer)};")
    }

    override fun visitIf(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.If) {
        writer.writeln("if(${evaluate(node.cond)}) {")
        writer.indent()

        node.then.statements.forEach { execute(it) }

        if (node.elseBlock != null) {
            writer.unindent()
            writer.writeln("} else {")
            writer.indent()
            node.elseBlock!!.statements.forEach { execute(it) }
        }

        writer.unindent()
        writer.writeln("}")
    }

    override fun visitFor(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.For) {
        val initStr = when (val s = node.init) {
            is dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarDeclaration -> "${s.type} ${s.identifier} = ${evaluate(s.initializer)}"
            is dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment -> "${s.identifier} = ${evaluate(s.expression)}"
            is dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.ExpressionStatement -> evaluate(s.expression)
            else -> ""
        }
        val condStr = evaluate(node.condition)
        val stepStr = when (val s = node.step) {
            is dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.ExpressionStatement -> evaluate(s.expression)
            is dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment -> "${s.identifier} = ${evaluate(s.expression)}"
            else -> ""
        }
        writer.writeln("for ($initStr; $condStr; $stepStr) {")
        writer.indent()
        node.body.statements.forEach { execute(it) }
        writer.unindent()
        writer.writeln("}")
    }

    override fun visitWhile(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.While) {
        writer.writeln("while (${evaluate(node.condition)}) {")
        writer.indent()
        node.body.statements.forEach { execute(it) }
        writer.unindent()
        writer.writeln("}")
    }

    override fun visitExpressionStatement(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.ExpressionStatement) {
        writer.writeln("${evaluate(node.expression)};")
    }

    override fun visitFunctionCall(node: FunctionCall): String {
        return "${node.identifier}(${node.expressions.joinToString(", ") { evaluate(it) }})"
    }

    override fun visitMemberCall(node: dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.MemberCall): String {
        val recv = evaluate(node.receiver)
        val recvFormatted = if (node.receiver is BinaryOperator) "($recv)" else recv
        val args = node.arguments.joinToString(", ") { evaluate(it) }
        return "$recvFormatted.${node.methodName}($args)"
    }

    override fun visitIdentifier(node: Identifier): String {
        return node.identifier
    }

    override fun visitLiteral(node: Literal): String {
        return node.literal
    }

    private fun precedence(op: String): Int = when (op) {
        "*", "/" -> 4
        "%", "+", "-" -> 3
        "<<", ">>" -> 2
        "<", ">", "<=", ">=" -> 1
        "==", "!=" -> 0
        "&" -> -1
        "^" -> -2
        "|" -> -3
        "&&" -> -4
        "||" -> -5
        else -> 0
    }

    override fun visitBinaryOperator(node: BinaryOperator): String {
        val parentPrec = precedence(node.op)
        val leftStr = evaluate(node.left)
        val leftFormatted = if (node.left is BinaryOperator && precedence(node.left.op) < parentPrec) {
            "($leftStr)"
        } else {
            leftStr
        }

        val rightStr = evaluate(node.right)
        val rightFormatted = if (node.right is BinaryOperator && (precedence(node.right.op) < parentPrec || (precedence(node.right.op) == parentPrec && (node.op == "-" || node.op == "/")))) {
            "($rightStr)"
        } else {
            rightStr
        }

        return "$leftFormatted ${node.op} $rightFormatted"
    }

    override fun visitUnaryOperator(node: UnaryOperator): String {
        val exp = evaluate(node.expression)
        return if (node.prefix) {
            if (node.expression is BinaryOperator) "${node.op}($exp)" else "${node.op}$exp"
        } else {
            "$exp${node.op}"
        }
    }

    override fun visitFieldAccess(node: FieldAccess): String {
        val exp = evaluate(node.expression)
        return if (node.expression is BinaryOperator) "($exp).${node.field}" else "$exp.${node.field}"
    }

    override fun visitIndexAccess(node: IndexAccess): String {
        val exp = evaluate(node.expression)
        val idx = evaluate(node.index)
        return if (node.expression is BinaryOperator) "($exp)[$idx]" else "$exp[$idx]"
    }

    override fun getResult(): String {
        return writer.toString()
    }
}
