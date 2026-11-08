# KShader

> ⚠️ **Notice: Prototype & Active Refactoring**  
> This project is currently an experimental prototype. The codebase is under active development, requires refactoring, and the API is not yet stable.

**KShader** is an embedded type-safe Kotlin DSL for authoring [SkSL (Skia Shading Language)](https://skia.org/docs/user/sksl/) shaders in Kotlin Multiplatform and Compose Multiplatform projects.

---

## 💡 Why KShader?

In Compose Multiplatform (Desktop, Android, WebAssembly) and Skiko applications, shaders are traditionally authored as **raw strings** passed directly to `RuntimeEffect.makeForShader(...)`.

### The Problem with Raw Strings

- ❌ **No Compile-Time Checking:** A simple typo in a variable or uniform name, a missing semicolon, an invalid swizzle (e.g. `.xyw` on a `float2`), or a type mismatch will compile fine in Kotlin, but crash your application at runtime when Skia attempts to parse the shader.
- ❌ **No IDE Autocomplete:** Writing SkSL inside a multi-line string means losing code completion for types, vector swizzles, and math intrinsics.
- ❌ **Fragile Maintenance:** Refactoring code or renaming uniforms requires manual search-and-replace inside string literals with no compiler guarantees.

### What KShader Provides

KShader provides a strongly-typed Kotlin DSL that represents the SkSL AST:
- **Compile-Time Safety:** Vector dimensions, matrix operations, scalar types, and swizzles are checked by the Kotlin compiler before your code ever runs.
- **IDE Assistance:** Autocomplete for SkSL types (`SFloat3`, `SHalf4`, `float2x2`), math intrinsics (`sin`, `cos`, `length`, `dot`), and swizzling.
- **Clean SkSL Generation:** The DSL builds an AST and generates clean, valid SkSL ready for `RuntimeEffect.makeForShader(...)`.

---

## 🔍 Code Comparison

Below is a comparison of authoring the same 3D raymarching tunnel shader:

### Before: Raw String SkSL

```kotlin
// The traditional way: shader logic inside an unchecked string
val rawSksl = """
    uniform float iTime;
    uniform float2 iResolution;

    float f(float3 p) {
        float p_z = p.z - iTime * 10.0;
        float a = p_z * 0.1;
        float2 p_xy = p.xy * mat2(cos(a), sin(a), -sin(a), cos(a));
        float qwe = 0.1 - length(cos(p_xy) + sin(float2(p.y, p_z)));
        return qwe;
    }

    half4 main(float2 fragcoord) {
        float3 d = 0.5 - float3(fragcoord.xy, 1.0) / iResolution.y;
        float3 p = float3(0.0);
        for (int i = 0; i < 32; i++) {
            p += f(p) * d;
        }
        float3 col = (sin(p) + float3(2.0, 5.0, 12.0)) / length(p);
        return half4(half3(col), 1.0);
    }
""".trimIndent()

// Any syntax error or type mismatch here only crashes at runtime:
val effect = RuntimeEffect.makeForShader(rawSksl)
```

### After: With KShader DSL

```kotlin
// The KShader way: type-safe Kotlin DSL
val notargsShader = shader {
    val iTime by uniform(SFloat)
    val iResolution by uniform(SFloat2)

    val f by fn(SFloat3, SFloat) { p ->
        val p_z by p.z - iTime * 10.0.f
        val a by p_z * 0.1.f
        val p_xy by p.xy * float2x2(cos(a), sin(a), -sin(a), cos(a))
        val qwe by 0.1.f - length(cos(p_xy) + sin(float2(p.y, p_z)))
        returns(qwe)
    }

    main { fragcoord ->
        val d by 0.5.f - float3(fragcoord.xy, 1.0.f) / iResolution.y
        var p by float3(0.0.f)
        loop(0 until 32) {
            p += f(p) * d
        }
        val col by (sin(p) + float3(2.0.f, 5.0.f, 12.0.f)) / length(p)
        half4(col.h, 1.0.h)
    }
}

val effect = RuntimeEffect.makeForShader(notargsShader.toSkslString())
```

---

## 🚀 Live Preview

A live WebAssembly preview running the raymarching tunnel shader is hosted on **GitHub Pages**:

👉 **[Launch Live Demo](https://emwi-zzz.github.io/KShader/)**  
*(Click **"▶ Run Render"** on the page to start the shader)*

---

## 🛠️ Running the Wasm Sample Locally

The Wasm sample in `samples/wasm` is an independent Gradle project. You can run it locally with live reloading:

```bash
./gradlew -p samples/wasm wasmJsBrowserDevelopmentRun
```
*(or navigate to `cd samples/wasm && ./gradlew wasmJsBrowserDevelopmentRun`)*

To build the static distribution for GitHub Pages:
```bash
./gradlew -p samples/wasm wasmJsBrowserDistribution
```

---

## 📂 Project Structure

- [`core`](core): Kotlin Multiplatform core library containing the AST, scopes, and types for the eDSL.
- [`sksl-typegen`](sksl-typegen): Code generation tool producing type-safe wrappers for SkSL types, intrinsics, and operations.
- [`samples/wasm`](samples/wasm): Independent sample project rendering the shader via Compose Multiplatform on WebAssembly.
