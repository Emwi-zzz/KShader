package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression

internal data class While(
    val condition: Expression,
    val body: Block
) : Statement
