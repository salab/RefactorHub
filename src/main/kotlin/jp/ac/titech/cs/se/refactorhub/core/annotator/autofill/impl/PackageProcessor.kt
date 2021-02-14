package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class PackageProcessor : AutofillProcessor<Package> {
    override fun process(
        autofill: Package,
        sourceCategory: DiffCategory,
        sourceElement: CodeElement,
        targetCategory: DiffCategory,
        targetMetadata: CodeElementMetadata,
        content: CommitContent
    ): List<CodeElement> {
        val file = content.files.get(targetCategory).find { it.name == sourceElement.location?.path }
        return file?.content?.elements?.filter { it.type == CodeElementType.PackageDeclaration } ?: listOf()
    }
}
