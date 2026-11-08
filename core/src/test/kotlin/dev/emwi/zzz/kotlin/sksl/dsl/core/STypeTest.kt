package dev.emwi.zzz.kotlin.sksl.dsl.core

import dev.emwi.zzz.kotlin.sksl.dsl.core.type.SType
import dev.emwi.zzz.kotlin.sksl.dsl.core.type.STypeCompanion
import kotlin.test.Test
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.isSubclassOf

class STypeTest {
    @Test
    fun `SType inheritor companion objects implement STypeCompanion`() {
        SType::class.sealedSubclasses.forEach {
            assert(it.companionObject?.isSubclassOf(STypeCompanion::class)?: false)
        }
    }
}