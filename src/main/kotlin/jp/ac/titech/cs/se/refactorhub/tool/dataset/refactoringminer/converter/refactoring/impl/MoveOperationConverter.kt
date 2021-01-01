package jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.MoveOperationRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class MoveOperationConverter :
    RefactoringConverter<MoveOperationRefactoring> {
    override fun convert(
        refactoring: MoveOperationRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.originalOperation))
                    )
                ),
                mapOf(
                    "moved method" to CodeElementHolder(
                        type = CodeElementType.MethodDeclaration,
                        elements = listOf(convertElement(refactoring.movedOperation))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
