package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.ExtractVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertCodeElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class ExtractVariableConverter :
    RefactoringConverter<ExtractVariableRefactoring> {
    override fun convert(
        refactoring: ExtractVariableRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        println("references: ${refactoring.references}")
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
                    ),
                    "extracted variable" to CodeElementHolder(
                        type = CodeElementType.VariableDeclaration,
                        elements = listOf(convertCodeElement(refactoring.variableDeclaration))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
