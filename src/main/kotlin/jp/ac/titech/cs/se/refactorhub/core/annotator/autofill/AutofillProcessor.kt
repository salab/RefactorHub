package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl.PackageProcessor
import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl.ReferenceProcessor
import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl.SurroundProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.Autofill
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Package
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Surround
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement

interface AutofillProcessor<T : Autofill> {
    fun process(
        autofill: T,
        follow: CodeElement,
        category: DiffCategory,
        contents: CommitFileContents
    ): List<CodeElement>
}

fun autofill(
    autofill: Autofill,
    follow: CodeElement,
    category: DiffCategory,
    contents: CommitFileContents
): List<CodeElement> {
    return when (autofill) {
        is Package -> PackageProcessor().process(autofill, follow, category, contents)
        is Reference -> ReferenceProcessor().process(autofill, follow, category, contents)
        is Surround -> SurroundProcessor().process(autofill, follow, category, contents)
        else -> listOf()
    }
}
