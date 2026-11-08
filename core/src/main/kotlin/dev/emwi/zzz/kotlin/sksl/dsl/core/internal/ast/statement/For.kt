package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression

internal data class For(
    val init: Statement,
    val condition: Expression,
    val step: Statement,
    val body: Block
) : Statement
