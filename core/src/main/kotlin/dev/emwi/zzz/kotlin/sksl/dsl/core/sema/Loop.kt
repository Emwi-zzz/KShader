package dev.emwi.zzz.kotlin.sksl.dsl.core.sema

import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.BinaryOperator
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Identifier
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.Literal
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.expression.UnaryOperator
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.ExpressionStatement
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.For
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarAssignment
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.VarDeclaration
import dev.emwi.zzz.kotlin.sksl.dsl.core.internal.ast.statement.While
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.LoopScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.scope.ShaderScope
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SBool
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SInt

context(outerScope: ShaderScope)
fun loop(condition: SBool, init: LoopScope.() -> Unit) {
    LoopScope.use{
        init()
        outerScope.addStatement(
            While(
                condition.exp,
                block
            )
        )
    }
}



context(outerScope: ShaderScope)
fun loop(range: IntRange, body: LoopScope.(SInt) -> Unit){
    LoopScope.use{
        val id = ctx.nextLoopVarName()
        val i = SInt(Identifier(id))
        body(i)
        outerScope.addStatement(
            For(
                VarDeclaration(
                    "int",
                    id,
                    Literal(range.first.toString())
                ),
                BinaryOperator(
                    i.exp,
                    Literal((range.last + 1).toString()),
                    op = "<"
                ),
                ExpressionStatement(
                    UnaryOperator(
                        i.exp,
                        op = "++",
                        prefix = false
                    )
                ),
                block
            )
        )
    }
}