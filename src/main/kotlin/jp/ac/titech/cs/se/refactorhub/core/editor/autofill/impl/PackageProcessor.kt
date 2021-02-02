package jp.ac.titech.cs.se.refactorhub.core.editor.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.editor.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.impl.Package
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
