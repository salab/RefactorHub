package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata

class PackageProcessor : AutofillProcessor<Package> {
    override fun process(
        autofill: Package,
        sourceCategory: DiffCategory,
        sourceElement: CodeElement,
        targetCategory: DiffCategory,
        targetMetadata: CodeElementMetadata,
        getFileCodeElementsMap: (DiffCategory) -> Map<String, List<CodeElement>>,
        isInDiffHunk: (DiffCategory, filePath: String, queryLineNumber: Int) -> Boolean
    ): List<CodeElement> {
        return sourceElement.location?.path
            ?.let { getFileCodeElementsMap(targetCategory)[it] }
            ?.filter { it.type == targetMetadata.type } ?: listOf()
    }
}
