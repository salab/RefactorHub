package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.MoveAttributeRefactoring
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.RefactoringOracle
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertCodeElement
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

class MoveAttributeConverter :
    RefactoringConverter<MoveAttributeRefactoring> {
    override fun convert(
        refactoring: MoveAttributeRefactoring,
        data: RefactoringOracle.Refactoring
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mutableMapOf(
                    "target field" to CodeElementHolder(
                        type = CodeElementType.FieldDeclaration,
                        elements = listOf(convertCodeElement(refactoring.originalAttribute))
                    )
                ),
                mutableMapOf(
                    "moved field" to CodeElementHolder(
                        type = CodeElementType.FieldDeclaration,
                        elements = listOf(convertCodeElement(refactoring.movedAttribute))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
