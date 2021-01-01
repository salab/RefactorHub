package jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType
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
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationBefore))
                    ),
                    when (refactoring.refactoringType) {
                        RefactoringType.RENAME_VARIABLE, RefactoringType.PARAMETERIZE_VARIABLE -> "target variable" to CodeElementHolder(
                            type = CodeElementType.VariableDeclaration,
                            elements = listOf(convertElement(refactoring.originalVariable))
                        )
                        RefactoringType.RENAME_PARAMETER -> "target parameter" to CodeElementHolder(
                            type = CodeElementType.ParameterDeclaration,
                            elements = listOf(convertElement(refactoring.originalVariable))
                        )
                        else -> throw RuntimeException("Converter for ${refactoring.refactoringType.displayName} is not implemented.")
                    }
                ),
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.operationAfter))
                    ),
                    when (refactoring.refactoringType) {
                        RefactoringType.RENAME_VARIABLE -> "target variable" to CodeElementHolder(
                            type = CodeElementType.VariableDeclaration,
                            elements = listOf(convertElement(refactoring.originalVariable))
                        )
                        RefactoringType.RENAME_PARAMETER, RefactoringType.PARAMETERIZE_VARIABLE -> "target parameter" to CodeElementHolder(
                            type = CodeElementType.ParameterDeclaration,
                            elements = listOf(convertElement(refactoring.originalVariable))
                        )
                        else -> throw RuntimeException("Converter for ${refactoring.refactoringType.displayName} is not implemented.")
                    }
                )
            ),
            refactoring.toString()
        )
    }
}
