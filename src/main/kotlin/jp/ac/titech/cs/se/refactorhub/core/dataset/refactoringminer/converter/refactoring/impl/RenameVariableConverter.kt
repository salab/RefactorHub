package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertCodeElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import org.refactoringminer.api.RefactoringType

class RenameVariableConverter :
    RefactoringConverter<RenameVariableRefactoring> {
    override fun convert(
        refactoring: RenameVariableRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.operationBefore))
                    ),
                    when (refactoring.refactoringType) {
                        RefactoringType.RENAME_VARIABLE, RefactoringType.PARAMETERIZE_VARIABLE -> "target variable" to CodeElementHolder(
                            type = CodeElementType.VariableDeclaration,
                            elements = listOf(convertCodeElement(refactoring.originalVariable))
                        )
                        RefactoringType.RENAME_PARAMETER -> "target parameter" to CodeElementHolder(
                            type = CodeElementType.ParameterDeclaration,
                            elements = listOf(convertCodeElement(refactoring.originalVariable))
                        )
                        else -> throw RuntimeException("Converter for ${refactoring.refactoringType.displayName} is not implemented.")
                    }
                ),
                mutableMapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertCodeElement(refactoring.operationAfter))
                    ),
                    when (refactoring.refactoringType) {
                        RefactoringType.RENAME_VARIABLE -> "target variable" to CodeElementHolder(
                            type = CodeElementType.VariableDeclaration,
                            elements = listOf(convertCodeElement(refactoring.originalVariable))
                        )
                        RefactoringType.RENAME_PARAMETER, RefactoringType.PARAMETERIZE_VARIABLE -> "target parameter" to CodeElementHolder(
                            type = CodeElementType.ParameterDeclaration,
                            elements = listOf(convertCodeElement(refactoring.originalVariable))
                        )
                        else -> throw RuntimeException("Converter for ${refactoring.refactoringType.displayName} is not implemented.")
                    }
                )
            ),
            refactoring.toString()
        )
    }
}
