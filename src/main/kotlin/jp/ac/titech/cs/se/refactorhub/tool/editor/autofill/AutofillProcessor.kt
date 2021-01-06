package jp.ac.titech.cs.se.refactorhub.tool.editor.autofill

import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl.PackageProcessor
import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl.ReferenceProcessor
import jp.ac.titech.cs.se.refactorhub.tool.editor.autofill.impl.SurroundProcessor
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.Autofill
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Surround
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement

interface AutofillProcessor<T : Autofill> {
    fun process(autofill: T, follow: CodeElement, contents: CommitFileContents): List<CodeElement>
}

fun autofill(autofill: Autofill, follow: CodeElement, contents: CommitFileContents): List<CodeElement> {
    return when (autofill) {
        is Package -> PackageProcessor().process(autofill, follow, contents)
        is Reference -> ReferenceProcessor().process(autofill, follow, contents)
        is Surround -> SurroundProcessor().process(autofill, follow, contents)
        else -> listOf()
    }
}
