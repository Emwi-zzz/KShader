package dev.emwi.zzz.kotlin.sksl.dsl.core.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.FunctionDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Parameter
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ScopeCompanion
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ShaderContext
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.processReturnBlock
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SFloat2
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SHalf4

@ShaderDsl
class GlobalShaderScope private constructor(ctx: ShaderContext) : ShaderScope(ctx) {

    val main: Nothing
        get() = error("Unavailable")
    fun main(init: FunctionScope<SHalf4>.(fragCoord: SFloat2) -> SHalf4) {
        FunctionScope.use<Unit, SHalf4> {
            processReturnBlock { init(SFloat2(Identifier("fragCoord"))) }
            this@GlobalShaderScope.addStatement(
                FunctionDeclaration(
                    "main",
                    SHalf4.typeName,
                    listOf(
                        Parameter(
                            "fragCoord",
                            SFloat2.typeName
                        )
                    ),
                    block
                )
            )
        }
    }

    internal companion object : ScopeCompanion<GlobalShaderScope>(::GlobalShaderScope) {
        operator fun <R> invoke(ctx: ShaderContext, block: GlobalShaderScope.() -> R): R {
            val scope = factory(ctx)
            scope.ctx.enterScope(scope)
            return try {
                block(scope)
            } finally {
                scope.ctx.exitScope()
            }
        }
    }
}
