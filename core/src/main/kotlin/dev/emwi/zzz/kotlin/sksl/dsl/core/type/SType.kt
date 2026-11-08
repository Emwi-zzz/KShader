package dev.emwi.zzz.kotlin.sksl.dsl.core.type

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Expression
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope
import kotlin.reflect.KProperty

sealed class STypeCompanion<T : SType<T>> {
    abstract val typeName: String
    internal abstract fun createInstance(exp: Expression): T
}

sealed class SType<T : SType<T>>(
    internal val exp: Expression
) {
    internal abstract fun copyWithExp(exp: Expression): T
    abstract val typeName: String
}

class SDelegate<T : SType<T>>(private val value: T, private val getCurrentScope: () -> ShaderScope) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T = value
    operator fun setValue(thisRef: Nothing?, property: KProperty<*>, value: T) {
        val declaration =
            VarAssignment(property.name, value.exp)
        getCurrentScope().addStatement(declaration)
    }
}