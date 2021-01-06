package jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl

import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class PackageProcessor : AutofillProcessor<Package> {
    override fun process(autofill: Package, follow: CodeElement, contents: CommitFileContents): List<CodeElement> {
        val file = contents.files.find { it.name == follow.location?.path }
        return file?.content?.elements?.filter { it.type == CodeElementType.PackageDeclaration } ?: listOf()
    }
}
