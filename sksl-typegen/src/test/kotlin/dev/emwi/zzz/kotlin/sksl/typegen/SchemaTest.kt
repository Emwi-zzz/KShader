package dev.emwi.zzz.kotlin.sksl.typegen

import dev.emwi.zzz.kotlin.sksl.typegen.schema.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaTest {

    @Test
    fun `schema contains all expected families`() {
        val familyNames = SkslTypes.families.map { it.skslName }.toSet()
        val expected = setOf("float", "half", "int", "short", "uint", "ushort", "bool")
        assertEquals(expected, familyNames)
    }

    @Test
    fun `schema contains all expected opaque objects`() {
        val opaqueNames = SkslTypes.opaques.map { it.skslName }.toSet()
        val expected = setOf("shader", "colorFilter", "blender")
        assertEquals(expected, opaqueNames)
    }

    @Test
    fun `floating point preset has expected shapes and ops`() {
        val fp = FloatingPoint
        assertTrue(fp.shapes.any { it is Scalar })
        assertTrue(fp.shapes.any { it is Vector })
        assertTrue(fp.shapes.any { it is Matrix })
        assertTrue(fp.ops.contains(Op.MatrixMul))
        assertTrue(fp.ops.contains(Op.Add))
    }
}
