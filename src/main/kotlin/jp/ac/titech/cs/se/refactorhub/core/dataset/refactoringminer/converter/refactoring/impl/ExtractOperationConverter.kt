package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertCodeElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class ExtractOperationConverter :
    RefactoringConverter<ExtractOperationRefactoring> {
    override fun convert(
        refactoring: ExtractOperationRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.sourceOperationBeforeExtraction))
                    ),
                    "extracted code" to CodeElementHolder(
                        type = CodeElementType.CodeFragment,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsFromSourceOperation.map { convertCodeElement(it) }
                    )
                ),
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.sourceOperationAfterExtraction))
                    ),
                    "extracted method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.extractedOperation))
                    ),
                    "invocation" to CodeElementHolder(
                        type = CodeElementType.MethodInvocation,
                        elements = refactoring.extractedOperationInvocations.map { convertCodeElement(it) }
                    ),
                    "extracted code" to CodeElementHolder(
                        type = CodeElementType.CodeFragment,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsToExtractedOperation.map { convertCodeElement(it) }
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
