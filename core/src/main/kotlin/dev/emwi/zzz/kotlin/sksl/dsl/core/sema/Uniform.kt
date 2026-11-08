package dev.emwi.zzz.kotlin.sksl.dsl.core.sema

import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.GlobalShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.UniformDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SDelegate
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SType
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.STypeCompanion
import kotlin.reflect.KProperty

class UniformDelegate<T : SType<T>> internal constructor(
    private val scope: GlobalShaderScope,
    private val token: STypeCompanion<T>
) {
    operator fun provideDelegate(thisRef: ShaderScope?, property: KProperty<*>): SDelegate<T> {
        val name = property.name
        scope.addStatement(
            UniformDeclaration(
                name,
                token.typeName
            )
        )
        return SDelegate(token.createInstance(Identifier(name)), scope.ctx::getScope)
    }
}

context(scope: GlobalShaderScope)
fun <T : SType<T>> uniform(token: STypeCompanion<T>, name: String): T {
    scope.addStatement(
        UniformDeclaration(
            name,
            token.typeName
        )
    )
    return token.createInstance(Identifier(name))
}

context(scope: GlobalShaderScope)
fun <T : SType<T>> uniform(token: STypeCompanion<T>): UniformDelegate<T> {
    return UniformDelegate(scope, token)
}
