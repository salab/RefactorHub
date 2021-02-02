package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class PackageProcessor : AutofillProcessor<Package> {
    override fun process(
        autofill: Package,
        follow: CodeElement,
        category: DiffCategory,
        contents: CommitFileContents
    ): List<CodeElement> {
        val file = contents.files.get(category).find { it.name == follow.location?.path }
        return file?.content?.elements?.filter { it.type == CodeElementType.PackageDeclaration } ?: listOf()
    }
}
