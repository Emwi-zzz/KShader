package dev.emwi.zzz.kotlin.sksl.dsl.core.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ScopeCompanion
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ShaderContext

class IfScope internal constructor(ctx: ShaderContext) : ShaderScope(ctx) {
    internal companion object : ScopeCompanion<IfScope>(::IfScope)
}
