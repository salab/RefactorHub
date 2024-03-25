package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Name
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration

class ReferenceProcessor : AutofillProcessor<Reference> {
    override fun process(
        autofill: Reference,
        sourceCategory: DiffCategory,
        sourceElement: CodeElement,
        targetCategory: DiffCategory,
        targetMetadata: CodeElementMetadata,
        getFileCodeElementsMap: (DiffCategory) -> Map<String, List<CodeElement>>,
        isInDiffHunk: (DiffCategory, filePath: String, queryLineNumber: Int) -> Boolean
    ): List<CodeElement> {
        val name = when (sourceElement) {
            is ClassDeclaration -> sourceElement.name
            is FieldDeclaration -> sourceElement.name
            is VariableDeclaration -> sourceElement.name
            else -> return listOf() // TODO
        }
        // TODO
        return getFileCodeElementsMap(targetCategory).map { (filePath, elements) ->
            elements.filter { element ->
                element.type == targetMetadata.type && element is Name && element.name == name
            }.filter { element ->
                val start = element.location?.range?.startLine
                val end = element.location?.range?.endLine
                (start != null && isInDiffHunk(targetCategory, filePath, start))
                        || (end != null && isInDiffHunk(targetCategory, filePath, end))
            }
        }.flatten()
    }
}
