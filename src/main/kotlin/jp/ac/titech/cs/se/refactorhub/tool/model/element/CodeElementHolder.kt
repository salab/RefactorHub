package jp.ac.titech.cs.se.refactorhub.tool.model.element

data class CodeElementHolder(
    val type: CodeElementType,
    val multiple: Boolean = false,
    val elements: List<CodeElement> = listOf(),
    val state: State = State.None
) {
    enum class State {
        None, Autofill, Manual
    }
}
