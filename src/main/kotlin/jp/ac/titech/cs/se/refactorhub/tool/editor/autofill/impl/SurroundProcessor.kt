package jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl

import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Surround
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Range

class SurroundProcessor : AutofillProcessor<Surround> {
    override fun process(autofill: Surround, follow: CodeElement, contents: CommitFileContents): List<CodeElement> {
        val file = contents.files.find { it.name == follow.location?.path } ?: return listOf()
        val range = follow.location?.range ?: return listOf()
        val elements = file.content.elements.filter {
            it.location?.range?.contains(range) ?: false
        }
        return elements.filter {
            it.location?.range?.let { r ->
                elements.all { other ->
                    other.location?.range?.contains(r) ?: false
                }
            } ?: false
        }
    }

    private fun Range.contains(range: Range): Boolean {
        return !(
            (range.startLine < startLine || range.endLine < startLine)
                || (range.startLine > endLine || range.endLine > endLine)
                || (range.startLine == startLine && range.startColumn <= startColumn)
                || (range.endLine == endLine || range.endColumn >= endColumn)
            )
    }
}
