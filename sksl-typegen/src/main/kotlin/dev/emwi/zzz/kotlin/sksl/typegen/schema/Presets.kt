package dev.emwi.zzz.kotlin.sksl.typegen.schema

val FloatingPoint = preset {
    shapes(Scalar, Vector(2..4), Matrix(2..4))
    ops(Add, Sub, Mul, Div, Negate, Compare, Equal, MatrixMul)
    intrinsics(TrigMath, CommonMath, GeometricMath, MatrixMath)
}

val SignedInt = preset {
    shapes(Scalar, Vector(2..4))
    ops(Add, Sub, Mul, Div, Mod, Negate, Bitwise, Compare, Equal)
    intrinsics(IntMath)
}

val UnsignedInt = preset {
    shapes(Scalar, Vector(2..4))
    ops(Add, Sub, Mul, Div, Mod, Bitwise, Compare, Equal)
    intrinsics(IntMath)
}

val BoolLogic = preset {
    shapes(Scalar, Vector(2..4))
    ops(Not, Logical, Equal)
    intrinsics(BoolMath)
}
