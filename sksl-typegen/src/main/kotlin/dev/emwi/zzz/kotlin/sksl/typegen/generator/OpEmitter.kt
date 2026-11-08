package dev.emwi.zzz.kotlin.sksl.typegen.generator

import dev.emwi.zzz.kotlin.sksl.typegen.schema.Op

object OpEmitter {

    // --- SCALAR OPERATORS ---

    fun emitScalarMembers(cb: CodeBuilder, ktName: String, ops: Set<Op>) {
        if (Op.Negate in ops) {
            cb.line("operator fun unaryMinus(): $ktName = $ktName(UnaryOperator(this.exp, \"-\"))")
        }
        if (Op.Not in ops) {
            cb.line("operator fun not(): $ktName = $ktName(UnaryOperator(this.exp, \"!\"))")
        }
        if (Op.Add in ops) {
            cb.line("operator fun plus(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"+\"))")
        }
        if (Op.Sub in ops) {
            cb.line("operator fun minus(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"-\"))")
        }
        if (Op.Mul in ops) {
            cb.line("operator fun times(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"*\"))")
        }
        if (Op.Div in ops) {
            cb.line("operator fun div(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"/\"))")
        }
        if (Op.Mod in ops) {
            cb.line("operator fun rem(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"%\"))")
        }
        if (Op.Bitwise in ops) {
            cb.line("infix fun and(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"&\"))")
            cb.line("infix fun or(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"|\"))")
            cb.line("infix fun xor(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"^\"))")
            cb.line("infix fun shl(other: SInt): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"<<\"))")
            cb.line("infix fun shr(other: SInt): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \">>\"))")
            cb.line("infix fun shl(other: Int): $ktName = $ktName(BinaryOperator(this.exp, Literal(other.toString()), \"<<\"))")
            cb.line("infix fun shr(other: Int): $ktName = $ktName(BinaryOperator(this.exp, Literal(other.toString()), \">>\"))")
            cb.line("fun inv(): $ktName = $ktName(UnaryOperator(this.exp, \"~\"))")
        }
        if (Op.Logical in ops) {
            cb.line("infix fun and(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"&&\"))")
            cb.line("infix fun or(other: $ktName): $ktName = $ktName(BinaryOperator(this.exp, other.exp, \"||\"))")
        }
        if (Op.Compare in ops) {
            cb.line("infix fun lt(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \"<\"))")
            cb.line("infix fun gt(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \">\"))")
            cb.line("infix fun le(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \"<=\"))")
            cb.line("infix fun ge(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \">=\"))")
        }
        if (Op.Equal in ops) {
            cb.line("infix fun eq(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \"==\"))")
            cb.line("infix fun neq(other: $ktName): SBool = SBool(BinaryOperator(this.exp, other.exp, \"!=\"))")
        }
    }

    // --- VECTOR OPERATORS ---

    fun emitVectorMembers(cb: CodeBuilder, vecName: String, scalarName: String, boolVecName: String, ops: Set<Op>) {
        if (Op.Negate in ops) {
            cb.line("operator fun unaryMinus(): $vecName = $vecName(UnaryOperator(this.exp, \"-\"))")
        }
        if (Op.Not in ops) {
            cb.line("operator fun not(): $vecName = $vecName(UnaryOperator(this.exp, \"!\"))")
        }

        // Symmetric and Asymmetric arithmetic
        val binaryOps = listOfNotNull(
            if (Op.Add in ops) Triple("plus", "+", "Add") else null,
            if (Op.Sub in ops) Triple("minus", "-", "Sub") else null,
            if (Op.Mul in ops) Triple("times", "*", "Mul") else null,
            if (Op.Div in ops) Triple("div", "/", "Div") else null,
            if (Op.Mod in ops) Triple("rem", "%", "Mod") else null,
        )

        for ((funcName, symbol, _) in binaryOps) {
            // (VecN, VecN) -> VecN
            cb.line("operator fun $funcName(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
            // (VecN, Scalar) -> VecN
            cb.line("operator fun $funcName(other: $scalarName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
        }

        if (Op.Bitwise in ops) {
            cb.line("infix fun and(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"&\"))")
            cb.line("infix fun and(other: $scalarName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"&\"))")
            cb.line("infix fun or(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"|\"))")
            cb.line("infix fun or(other: $scalarName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"|\"))")
            cb.line("infix fun xor(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"^\"))")
            cb.line("infix fun xor(other: $scalarName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"^\"))")
            cb.line("fun inv(): $vecName = $vecName(UnaryOperator(this.exp, \"~\"))")
        }

        if (Op.Logical in ops) {
            cb.line("infix fun and(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"&&\"))")
            cb.line("infix fun or(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"||\"))")
        }

        if (Op.Compare in ops) {
            cb.line("infix fun lessThan(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"lessThan\", listOf(this.exp, other.exp)))")
            cb.line("infix fun lessThanEqual(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"lessThanEqual\", listOf(this.exp, other.exp)))")
            cb.line("infix fun greaterThan(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"greaterThan\", listOf(this.exp, other.exp)))")
            cb.line("infix fun greaterThanEqual(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"greaterThanEqual\", listOf(this.exp, other.exp)))")
            cb.line("infix fun lt(other: $vecName): $boolVecName = lessThan(other)")
            cb.line("infix fun le(other: $vecName): $boolVecName = lessThanEqual(other)")
            cb.line("infix fun gt(other: $vecName): $boolVecName = greaterThan(other)")
            cb.line("infix fun ge(other: $vecName): $boolVecName = greaterThanEqual(other)")
        }

        if (Op.Equal in ops) {
            cb.line("infix fun equal(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"equal\", listOf(this.exp, other.exp)))")
            cb.line("infix fun notEqual(other: $vecName): $boolVecName = $boolVecName(FunctionCall(\"notEqual\", listOf(this.exp, other.exp)))")
            cb.line("infix fun eq(other: $vecName): $boolVecName = equal(other)")
            cb.line("infix fun neq(other: $vecName): $boolVecName = notEqual(other)")
        }
    }

    // Commuted top-level operators for vectors: (Scalar, VecN) -> VecN
    fun emitVectorCommuted(cb: CodeBuilder, vecName: String, scalarName: String, ops: Set<Op>) {
        val binaryOps = listOfNotNull(
            if (Op.Add in ops) Triple("plus", "+", "Add") else null,
            if (Op.Sub in ops) Triple("minus", "-", "Sub") else null,
            if (Op.Mul in ops) Triple("times", "*", "Mul") else null,
            if (Op.Div in ops) Triple("div", "/", "Div") else null,
            if (Op.Mod in ops) Triple("rem", "%", "Mod") else null,
        )
        for ((funcName, symbol, _) in binaryOps) {
            cb.line("operator fun $scalarName.$funcName(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
        }
        if (Op.Bitwise in ops) {
            cb.line("infix fun $scalarName.and(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"&\"))")
            cb.line("infix fun $scalarName.or(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"|\"))")
            cb.line("infix fun $scalarName.xor(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"^\"))")
        }
    }

    // --- MATRIX OPERATORS ---

    fun emitMatrixMembers(cb: CodeBuilder, matName: String, scalarName: String, vecName: String, ops: Set<Op>) {
        if (Op.Negate in ops) {
            cb.line("operator fun unaryMinus(): $matName = $matName(UnaryOperator(this.exp, \"-\"))")
        }

        // Component-wise Add, Sub, Div: (MatN, MatN) -> MatN and (MatN, Scalar) -> MatN
        val binaryOps = listOfNotNull(
            if (Op.Add in ops) Pair("plus", "+") else null,
            if (Op.Sub in ops) Pair("minus", "-") else null,
            if (Op.Div in ops) Pair("div", "/") else null,
        )
        for ((funcName, symbol) in binaryOps) {
            cb.line("operator fun $funcName(other: $matName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
            cb.line("operator fun $funcName(other: $scalarName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
        }

        // MatrixMul (*)
        if (Op.MatrixMul in ops) {
            // MatN * Scalar -> MatN
            cb.line("operator fun times(other: $scalarName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"*\"))")
            // MatN * VecN -> VecN
            cb.line("operator fun times(other: $vecName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"*\"))")
            // MatN * MatN -> MatN
            cb.line("operator fun times(other: $matName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"*\"))")
        }
    }

    // Commuted top-level operators for matrices:
    fun emitMatrixCommuted(cb: CodeBuilder, matName: String, scalarName: String, vecName: String, ops: Set<Op>) {
        // Scalar [+, -, /] MatN -> MatN
        val binaryOps = listOfNotNull(
            if (Op.Add in ops) Pair("plus", "+") else null,
            if (Op.Sub in ops) Pair("minus", "-") else null,
            if (Op.Div in ops) Pair("div", "/") else null,
        )
        for ((funcName, symbol) in binaryOps) {
            cb.line("operator fun $scalarName.$funcName(other: $matName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"$symbol\"))")
        }
        if (Op.MatrixMul in ops) {
            // Scalar * MatN -> MatN
            cb.line("operator fun $scalarName.times(other: $matName): $matName = $matName(BinaryOperator(this.exp, other.exp, \"*\"))")
            // VecN * MatN -> VecN (vector-matrix pre-multiplication)
            cb.line("operator fun $vecName.times(other: $matName): $vecName = $vecName(BinaryOperator(this.exp, other.exp, \"*\"))")
        }
    }
}
