package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ChangeReturnTypeRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertCodeElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class ChangeReturnTypeConverter :
    RefactoringConverter<ChangeReturnTypeRefactoring> {
    override fun convert(
        refactoring: ChangeReturnTypeRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        // TODO: originalType, changedType, returnReferences
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.operationBefore))
                    )
                ),
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.operationAfter))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
