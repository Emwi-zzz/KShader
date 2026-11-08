package dev.emwi.zzz.kotlin.sksl.typegen.schema

private fun unary(name: String, filter: ShapeFilter = ShapeFilter.VecAndScalar) =
    IntrinsicDef(name, listOf(Self), Self, filter)

private fun binary(name: String, filter: ShapeFilter = ShapeFilter.VecAndScalar) =
    IntrinsicDef(name, listOf(Self, Self), Self, filter)

val TrigMath = IntrinsicBundle(
    "TrigMath",
    listOf(
        unary("radians"),
        unary("degrees"),
        unary("sin"),
        unary("cos"),
        unary("tan"),
        unary("asin"),
        unary("acos"),
        IntrinsicDef("atan", listOf(Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("atan", listOf(Self, Self), Self, ShapeFilter.VecAndScalar),
        unary("sinh"),
        unary("cosh"),
        unary("tanh"),
        unary("asinh"),
        unary("acosh"),
        unary("atanh"),
        unary("exp"),
        unary("log"),
        unary("exp2"),
        unary("log2"),
        unary("sqrt"),
        unary("inversesqrt"),
        binary("pow")
    )
)

val CommonMath = IntrinsicBundle(
    "CommonMath",
    listOf(
        unary("abs"),
        unary("sign"),
        unary("floor"),
        unary("trunc"),
        unary("round"),
        unary("ceil"),
        unary("fract"),
        unary("saturate"),
        unary("dFdx"),
        unary("dFdy"),
        unary("fwidth"),
        // mod
        binary("mod"),
        IntrinsicDef("mod", listOf(Self, Scalar), Self, ShapeFilter.VectorOnly),
        // min
        binary("min"),
        IntrinsicDef("min", listOf(Self, Scalar), Self, ShapeFilter.VectorOnly),
        // max
        binary("max"),
        IntrinsicDef("max", listOf(Self, Scalar), Self, ShapeFilter.VectorOnly),
        // step
        binary("step"),
        IntrinsicDef("step", listOf(Scalar, Self), Self, ShapeFilter.VectorOnly),
        // clamp
        IntrinsicDef("clamp", listOf(Self, Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("clamp", listOf(Self, Scalar, Scalar), Self, ShapeFilter.VectorOnly),
        // mix
        IntrinsicDef("mix", listOf(Self, Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("mix", listOf(Self, Self, Scalar), Self, ShapeFilter.VectorOnly),
        IntrinsicDef("mix", listOf(Self, Self, SameDimBool), Self, ShapeFilter.VectorOnly),
        // smoothstep
        IntrinsicDef("smoothstep", listOf(Self, Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("smoothstep", listOf(Scalar, Scalar, Self), Self, ShapeFilter.VectorOnly)
    )
)

val GeometricMath = IntrinsicBundle(
    "GeometricMath",
    listOf(
        IntrinsicDef("length", listOf(Self), Scalar, ShapeFilter.VecAndScalar),
        IntrinsicDef("distance", listOf(Self, Self), Scalar, ShapeFilter.VecAndScalar),
        IntrinsicDef("dot", listOf(Self, Self), Scalar, ShapeFilter.VecAndScalar),
        IntrinsicDef("normalize", listOf(Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("faceforward", listOf(Self, Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("reflect", listOf(Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("refract", listOf(Self, Self, Scalar), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("cross", listOf(Self, Self), Self, ShapeFilter.Dim3VectorOnly)
    )
)

val MatrixMath = IntrinsicBundle(
    "MatrixMath",
    listOf(
        IntrinsicDef("matrixCompMult", listOf(Self, Self), Self, ShapeFilter.MatrixOnly),
        IntrinsicDef("inverse", listOf(Self), Self, ShapeFilter.MatrixOnly),
        IntrinsicDef("transpose", listOf(Self), Self, ShapeFilter.MatrixOnly),
        IntrinsicDef("determinant", listOf(Self), Scalar, ShapeFilter.MatrixOnly),
        IntrinsicDef("outerProduct", listOf(TypeRef.Vec(), TypeRef.Vec()), Mat, ShapeFilter.MatrixOnly)
    )
)

val IntMath = IntrinsicBundle(
    "IntMath",
    listOf(
        unary("abs"),
        unary("sign"),
        binary("min"),
        IntrinsicDef("min", listOf(Self, Scalar), Self, ShapeFilter.VectorOnly),
        binary("max"),
        IntrinsicDef("max", listOf(Self, Scalar), Self, ShapeFilter.VectorOnly),
        IntrinsicDef("clamp", listOf(Self, Self, Self), Self, ShapeFilter.VecAndScalar),
        IntrinsicDef("clamp", listOf(Self, Scalar, Scalar), Self, ShapeFilter.VectorOnly)
    )
)

val BoolMath = IntrinsicBundle(
    "BoolMath",
    listOf(
        IntrinsicDef("any", listOf(Self), SBool, ShapeFilter.VectorOnly),
        IntrinsicDef("all", listOf(Self), SBool, ShapeFilter.VectorOnly),
        IntrinsicDef("not", listOf(Self), Self, ShapeFilter.VectorOnly)
    )
)
