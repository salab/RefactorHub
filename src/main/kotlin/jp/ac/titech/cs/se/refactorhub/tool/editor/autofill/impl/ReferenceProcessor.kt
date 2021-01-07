package jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl

import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.tool.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.SimpleName
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableDeclaration

class ReferenceProcessor : AutofillProcessor<Reference> {
    override fun process(
        autofill: Reference,
        follow: CodeElement,
        category: DiffCategory,
        contents: CommitFileContents
    ): List<CodeElement> {
        val name = when (follow) {
            is ClassDeclaration -> follow.name
            is FieldDeclaration -> follow.name
            is VariableDeclaration -> follow.name
            else -> return listOf() // TODO
        }
        // TODO
        return contents.files.get(category).flatMap { it.content.elements }
            .filter { it is SimpleName && it.name == name }
    }
}
