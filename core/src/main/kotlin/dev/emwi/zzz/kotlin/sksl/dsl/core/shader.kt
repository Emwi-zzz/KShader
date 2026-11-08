package dev.emwi.zzz.kotlin.sksl.dsl.core

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ShaderContext
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.GlobalShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.processBlock
import dev.emwi.zzz.kotlin.sksl.dsl.core.visitor.PlainSkslVisitor
import dev.emwi.zzz.kotlin.sksl.dsl.core.visitor.ShaderAstVisitor

class Shader(internal val scope: GlobalShaderScope) {
    fun toSkslString(visitor: ShaderAstVisitor<String> = PlainSkslVisitor()): String {
        visitor.visitBlock(scope.block)
        return visitor.getResult()
    }
}

fun shader(init: GlobalShaderScope.() -> Unit): Shader {
    return GlobalShaderScope(ShaderContext()){
        this.processBlock(init)
        return@GlobalShaderScope Shader(this)
    }
}