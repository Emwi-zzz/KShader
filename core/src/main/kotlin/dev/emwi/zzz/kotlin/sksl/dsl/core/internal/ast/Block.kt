package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Statement

internal data class Block(val statements: MutableList<Statement> = mutableListOf()) {
    constructor(vararg statements: Statement) : this(statements.toMutableList())
}