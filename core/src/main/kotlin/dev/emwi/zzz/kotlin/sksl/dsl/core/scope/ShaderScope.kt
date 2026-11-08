package dev.emwi.zzz.kotlin.sksl.dsl.core.scope

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.Block
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Return
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.Statement
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.scope.ShaderContext
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SDelegate
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SType
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE)
annotation class ShaderDsl

sealed class ShaderScope(internal val ctx: ShaderContext) {
    internal val block = Block()

    internal fun addStatement(statement: Statement) {
        block.statements.add(statement)
    }

    operator fun <T : SType<T>> T.provideDelegate(thisRef: Nothing?, property: KProperty<*>): SDelegate<T> {
        val declaration = VarDeclaration(typeName, property.name, exp)
        ctx.getScope().addStatement(declaration)
        val successor = copyWithExp(Identifier(property.name))
        return SDelegate(successor, ctx::getScope)
    }
}

fun <T : ShaderScope, R : SType<R>> T.processReturnBlock(block: T.()->R){
    val r = block()
    addStatement(Return(r.exp))
}

fun <T : ShaderScope> T.processBlock(block: T.()->Unit){
    block()
}