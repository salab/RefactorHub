package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Surround
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Range

class SurroundProcessor : AutofillProcessor<Surround> {
    override fun process(
        autofill: Surround,
        follow: CodeElement,
        category: DiffCategory,
        contents: CommitFileContents
    ): List<CodeElement> {
        val file = contents.files.get(category).find { it.name == follow.location?.path } ?: return listOf()
        val range = follow.location?.range ?: return listOf()
        val elements = file.content.elements.filter {
            it.type == autofill.element && it.location?.range?.contains(range) ?: false
        }
        return elements.filter {
            it.location?.range?.let { r ->
                elements.filter { e -> it != e }.all { other ->
                    other.location?.range?.contains(r) ?: false
                }
            } ?: false
        }
    }

    companion object {
        private fun Range.contains(range: Range): Boolean {
            return !(
                (range.startLine < startLine || range.endLine < startLine) ||
                    (range.startLine > endLine || range.endLine > endLine) ||
                    (range.startLine == startLine && range.startColumn < startColumn) ||
                    (range.endLine == endLine && range.endColumn > endColumn)
                )
        }
    }
}
