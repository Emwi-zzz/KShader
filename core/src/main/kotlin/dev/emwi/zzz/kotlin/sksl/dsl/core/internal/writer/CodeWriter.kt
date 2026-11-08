package dev.emwi.zzz.kotlin.sksl.dsl.core.internal.writer

internal class CodeWriter(private val indentString: String = "    ") {
    private val sb = StringBuilder()
    private var indentLevel = 0
    private var atStartOfLine = true

    fun indent() { indentLevel++ }
    fun unindent() { if (indentLevel > 0) indentLevel-- }

    fun write(text: String) {
        if (atStartOfLine && text.isNotEmpty()) {
            sb.append(indentString.repeat(indentLevel))
            atStartOfLine = false
        }
        sb.append(text)
    }

    fun writeln(text: String = "") {
        write(text)
        sb.append('\n')
        atStartOfLine = true
    }

    inline fun writeBlock(header: String = "", body: () -> Unit) {
        if (header.isNotEmpty()) {
            writeln("$header {")
        } else {
            writeln("{")
        }
        indent()
        body()
        unindent()
        writeln("}")
    }

    override fun toString(): String = sb.toString()
}