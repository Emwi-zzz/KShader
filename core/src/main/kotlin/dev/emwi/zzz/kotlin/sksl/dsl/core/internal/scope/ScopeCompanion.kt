package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope

internal abstract class ScopeCompanion<out T : ShaderScope>(
    protected val factory: (ShaderContext) -> T
) {
    context(scope: ShaderScope)
    fun <R> use(block: T.() -> R): R {
        val scope = factory(scope.ctx)
        scope.ctx.enterScope(scope)
        return try {
            block(scope)
        } finally {
            scope.ctx.exitScope()
        }
    }
}


