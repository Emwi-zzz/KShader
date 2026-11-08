package dev.emwi.zzz.kotlin.sksl.typegen.schema

import kotlin.reflect.KClass

// --- Shapes ---
sealed interface ShapeSpec

object Scalar : ShapeSpec, TypeRef {
    override fun toString(): String = "Scalar"
}

data class Vector(val dims: IntRange) : ShapeSpec {
    constructor(vararg dimensions: Int) : this(dimensions.first()..dimensions.last())
}

data class Matrix(val dims: IntRange) : ShapeSpec {
    constructor(vararg dimensions: Int) : this(dimensions.first()..dimensions.last())
}

// --- Operators ---
enum class Op {
    Add, Sub, Mul, Div, Mod, Negate, Compare, Equal, Bitwise, Logical, Not, MatrixMul
}

val Add = Op.Add
val Sub = Op.Sub
val Mul = Op.Mul
val Div = Op.Div
val Mod = Op.Mod
val Negate = Op.Negate
val Compare = Op.Compare
val Equal = Op.Equal
val Bitwise = Op.Bitwise
val Logical = Op.Logical
val Not = Op.Not
val MatrixMul = Op.MatrixMul

// --- Symbolic Type References ---
sealed interface TypeRef {
    object Self : TypeRef {
        override fun toString(): String = "Self"
    }
    object SameDimBool : TypeRef {
        override fun toString(): String = "SameDimBool"
    }
    object BoolScalar : TypeRef {
        override fun toString(): String = "BoolScalar"
    }
    object Mat : TypeRef {
        override fun toString(): String = "Mat"
    }
    data class Vec(val dim: Int? = null) : TypeRef
    data class Explicit(val name: String) : TypeRef
}

val Self = TypeRef.Self
val SameDimBool = TypeRef.SameDimBool
val BoolScalar = TypeRef.BoolScalar
val SBool = TypeRef.BoolScalar
val Mat = TypeRef.Mat

// --- Intrinsic Definitions ---
enum class ShapeFilter {
    All,
    ScalarOnly,
    VectorOnly,
    MatrixOnly,
    VecAndScalar,
    Dim3VectorOnly
}

data class IntrinsicDef(
    val name: String,
    val params: List<TypeRef>,
    val returnType: TypeRef,
    val filter: ShapeFilter = ShapeFilter.All,
    val skslName: String = name
)

data class IntrinsicBundle(
    val name: String,
    val definitions: List<IntrinsicDef>
)

// --- Presets ---
data class Preset(
    val shapes: List<ShapeSpec>,
    val ops: Set<Op>,
    val intrinsics: List<IntrinsicBundle>
) {
    companion object {
        val EMPTY = Preset(emptyList(), emptySet(), emptyList())
    }
}

class PresetBuilder {
    private val shapes = mutableListOf<ShapeSpec>()
    private val ops = mutableSetOf<Op>()
    private val intrinsics = mutableListOf<IntrinsicBundle>()

    fun shapes(vararg shapes: ShapeSpec) {
        this.shapes.addAll(shapes)
    }

    fun ops(vararg ops: Op) {
        this.ops.addAll(ops)
    }

    fun intrinsics(vararg bundles: IntrinsicBundle) {
        this.intrinsics.addAll(bundles)
    }

    fun build() = Preset(shapes, ops, intrinsics)
}

fun preset(init: PresetBuilder.() -> Unit): Preset {
    val b = PresetBuilder()
    b.init()
    return b.build()
}

// --- Shader Types Schema ---
data class TypeFamily(
    val skslName: String,
    val ktName: String,
    val ext: String,
    val primitive: KClass<*>,
    val preset: Preset
)

data class OpaqueParam(val name: String, val type: String)
data class OpaqueMethod(val name: String, val params: List<OpaqueParam>, val returnType: String)
data class OpaqueType(val skslName: String, val ktName: String, val methods: List<OpaqueMethod>)

data class ShaderTypesSchema(
    val families: List<TypeFamily>,
    val opaques: List<OpaqueType>
)

class ShaderTypesBuilder {
    private val families = mutableListOf<TypeFamily>()
    private val opaques = mutableListOf<OpaqueType>()

    fun family(skslName: String, ktName: String, ext: String, primitive: KClass<*>, init: FamilyBuilder.() -> Unit) {
        val b = FamilyBuilder(skslName, ktName, ext, primitive)
        b.init()
        families.add(b.build())
    }

    fun opaque(skslName: String, ktName: String, init: OpaqueTypeBuilder.() -> Unit) {
        val b = OpaqueTypeBuilder(skslName, ktName)
        b.init()
        opaques.add(b.build())
    }

    fun build() = ShaderTypesSchema(families, opaques)
}

fun shaderTypes(init: ShaderTypesBuilder.() -> Unit): ShaderTypesSchema {
    val b = ShaderTypesBuilder()
    b.init()
    return b.build()
}

class FamilyBuilder(
    val skslName: String,
    val ktName: String,
    val ext: String,
    val primitive: KClass<*>
) {
    private var preset: Preset = Preset.EMPTY

    fun apply(preset: Preset) {
        this.preset = preset
    }

    fun build() = TypeFamily(skslName, ktName, ext, primitive, preset)
}

class OpaqueTypeBuilder(val skslName: String, val ktName: String) {
    private val methodBuilders = mutableListOf<OpaqueMethodBuilder>()

    fun fn(name: String, vararg params: Pair<String, String>): OpaqueMethodBuilder {
        val mb = OpaqueMethodBuilder(name, params.map { OpaqueParam(it.first, it.second) })
        methodBuilders.add(mb)
        return mb
    }

    fun build() = OpaqueType(skslName, ktName, methodBuilders.map { it.build() })
}

class OpaqueMethodBuilder(val name: String, val params: List<OpaqueParam>) {
    var returnType: String = "void"

    infix fun returns(type: String) {
        this.returnType = type
    }

    fun build() = OpaqueMethod(name, params, returnType)
}
