package newconsole.console

import newconsole.ui.CodeArea

class KtsCodeArea: CodeArea {
    constructor(text: String?): super(text)

    constructor(text: String?, style: TextFieldStyle?): super(text, style)

    override fun loadInfo() {
        keywords.addAll(
            "open",
            "operator",
            "override",
            "private",
            "protected",
            "sealed",
            "public",
            "class",
            "object",
            "import",
            "package",
            "value",
            "fun",
            "final",
            "infix",
            "inner",
            "suspend",
            "tailrec",
            "lateinit",
            "typealias",
            "val",
            "var",
            "constructor",
            "data",
            "companion",
            "crossinline",
            "noinline",
            "abstract",
            "annotation",
            "enum"
        )
        statements.addAll(
            "break",
            "continue",
            "do",
            "else",
            "if",
            "return",
            "throw",
            "for",
            "try",
            "catch",
            "finally",
            "typeof",
            "when",
            "while",
            "in",
            "!in",
            "is",
            "!is",
            "as",
            "as?",
            "by",
            "reified",
            "out",
            "vararg"
        )
        literals.addAll("null", "super", "this", "true", "false", "get", "set", "it", "field")
        specials.addAll("_autorun_event")
    }
}
