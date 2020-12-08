package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class ChangeVariableTypeConverter :
    RefactoringConverter<ChangeVariableTypeRefactoring> {
    override fun convert(
        refactoring: ChangeVariableTypeRefactoring,
        data: RefOracleData
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
