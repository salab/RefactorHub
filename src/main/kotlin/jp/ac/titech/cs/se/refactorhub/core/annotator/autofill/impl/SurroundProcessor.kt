package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Surround
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Range

class SurroundProcessor : AutofillProcessor<Surround> {
    override fun process(
        autofill: Surround,
        sourceCategory: DiffCategory,
        sourceElement: CodeElement,
        targetCategory: DiffCategory,
        targetMetadata: CodeElementMetadata,
        content: CommitContent
    ): List<CodeElement> {
        val file = content.files.map { it.get(targetCategory) }.find { it.name == sourceElement.location?.path }
            ?: return listOf()
        val range = sourceElement.location?.range ?: return listOf()
        val elements = file.content.elements.filter {
            it.type == targetMetadata.type && it.location?.range?.contains(range) ?: false
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
