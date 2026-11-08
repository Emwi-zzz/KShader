package dev.emwi.zzz.kotlin.sksl.dsl.core.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ScopeCompanion
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ShaderContext
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SType

@ShaderDsl
open class FunctionScope<R : SType<R>> internal constructor(ctx: ShaderContext) : ShaderScope(ctx) {
    infix fun returns(value: R) : R{
        return value
    }
    internal companion object {
        context(scope: ShaderScope)
        fun <R, T : SType<T>> use(block: FunctionScope<T>.() -> R): R {
            val scope = FunctionScope<T>(scope.ctx)
            scope.ctx.enterScope(scope)
            return try {
                block(scope)
            } finally {
                scope.ctx.exitScope()
            }
        }
    }
}
