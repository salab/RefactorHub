package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class ExtractOperationConverter :
    RefactoringConverter<ExtractOperationRefactoring> {
    override fun convert(
        refactoring: ExtractOperationRefactoring,
        data: RefOracleData
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        multiple = true,
                        elements = listOf(convertElement(refactoring.sourceOperationBeforeExtraction))
                    ),
                    "extracted code" to CodeElementHolder(
                        type = CodeElementType.CodeFragment,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsFromSourceOperation.map { convertElement(it) }
                    )
                ),
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        multiple = true,
                        elements = listOf(convertElement(refactoring.sourceOperationAfterExtraction))
                    ),
                    "extracted method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.extractedOperation))
                    ),
                    "extracted method invocation" to CodeElementHolder(
                        type = CodeElementType.MethodInvocation,
                        multiple = true,
                        elements = refactoring.extractedOperationInvocations.map { convertElement(it) }
                    ),
                    "extracted code" to CodeElementHolder(
                        type = CodeElementType.CodeFragment,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsToExtractedOperation.map { convertElement(it) }
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
