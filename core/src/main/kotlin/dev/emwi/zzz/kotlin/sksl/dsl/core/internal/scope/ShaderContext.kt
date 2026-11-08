package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope

internal class ShaderContext {
    private val scopeManager = ArrayDeque<ShaderScope>()
    private var loopDepth = 0

    fun nextLoopVarName(): String {
        val name = when (loopDepth) {
            0 -> "i"
            1 -> "j"
            2 -> "k"
            else -> "i$loopDepth"
        }
        loopDepth++
        return name
    }

    fun releaseLoopVar() {
        if (loopDepth > 0) loopDepth--
    }

    fun enterScope(scope: ShaderScope) {
        scopeManager.addLast(scope)
    }

    fun exitScope() {
        if (scopeManager.isEmpty()) {
            throw IllegalStateException("Cannot exit scope: scope stack is empty")
        }
        scopeManager.removeLast()
    }

    fun getScope(): ShaderScope {
        return scopeManager.lastOrNull()
            ?: throw IllegalStateException("No active ShaderScope found in context")
    }
}
