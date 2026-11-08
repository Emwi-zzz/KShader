package dev.emwi.zzz.kotlin.sksl.typegen.schema

val SkslTypes = shaderTypes {
    // 1. Floating-Point Families (Scalar + Vec2..4 + Mat2..4)
    family("float", "SFloat", ext = "f", Float::class) { apply(FloatingPoint) }
    family("half",  "SHalf",  ext = "h", Float::class) { apply(FloatingPoint) }

    // 2. Integer Families (Scalar + Vec2..4)
    family("int",    "SInt",    ext = "i",  Int::class)    { apply(SignedInt) }
    family("short",  "SShort",  ext = "s",  Short::class)  { apply(SignedInt) }
    family("uint",   "SUInt",   ext = "u",  UInt::class)   { apply(UnsignedInt) }
    family("ushort", "SUShort", ext = "us", UShort::class) { apply(UnsignedInt) }

    // 3. Boolean Family (Scalar + Vec2..4)
    family("bool", "SBool", ext = "b", Boolean::class) { apply(BoolLogic) }

    // 4. Skia Opaque Uniforms
    opaque("shader", "SShader") {
        fn("eval", "coords" to "float2") returns "half4"
    }
    opaque("colorFilter", "SColorFilter") {
        fn("eval", "color" to "half4") returns "half4"
    }
    opaque("blender", "SBlender") {
        fn("eval", "src" to "half4", "dst" to "half4") returns "half4"
    }
}
