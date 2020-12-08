package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ChangeReturnTypeRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class ChangeReturnTypeConverter :
    RefactoringConverter<ChangeReturnTypeRefactoring> {
    override fun convert(
        refactoring: ChangeReturnTypeRefactoring,
        data: RefOracleData
    ): Refactoring {
        // TODO: originalType, changedType, returnReferences
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationBefore))
                    )
                ),
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationAfter))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
