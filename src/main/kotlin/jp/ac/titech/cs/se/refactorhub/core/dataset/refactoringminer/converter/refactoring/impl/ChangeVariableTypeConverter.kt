package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class ChangeVariableTypeConverter :
    RefactoringConverter<ChangeVariableTypeRefactoring> {
    override fun convert(
        refactoring: ChangeVariableTypeRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        // TODO: variableReferences, relatedRefactorings
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationBefore))
                    ),
                    "target variable" to CodeElementHolder(
                        type = CodeElementType.VariableDeclaration,
                        elements = listOf(convertElement(refactoring.originalVariable))
                    )
                ),
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationAfter))
                    ),
                    "changed variable" to CodeElementHolder(
                        type = CodeElementType.VariableDeclaration,
                        elements = listOf(convertElement(refactoring.changedTypeVariable))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
