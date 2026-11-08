package dev.emwi.zzz.kotlin.sksl.dsl.core.sema

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.If
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.IfScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SBool

class OpenIf internal constructor(internal val elseBlockSetter: (block: Block) -> Unit) {
    context(scope: ShaderScope)
    infix fun otherwise(init: IfScope.() -> Unit) {
        IfScope.use {
            init()
            elseBlockSetter(block)
        }
    }
}

context(outerScope: ShaderScope)
fun sIf(condition: SBool, init: IfScope.() -> Unit): OpenIf {
    return IfScope.use {
        init()
        val ifStatement = If(
            cond = condition.exp,
            then = block
        )
        outerScope.addStatement(ifStatement)
        OpenIf { elseBlock ->
            ifStatement.elseBlock = elseBlock
        }
    }
}
