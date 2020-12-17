package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.MoveAttributeRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class MoveAttributeConverter :
    RefactoringConverter<MoveAttributeRefactoring> {
    override fun convert(
        refactoring: MoveAttributeRefactoring,
        data: RefOracleData
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target field" to CodeElementHolder(
                        type = CodeElementType.FieldDeclaration,
                        elements = listOf(convertElement(refactoring.originalAttribute))
                    )
                ),
                mapOf(
                    "moved field" to CodeElementHolder(
                        type = CodeElementType.FieldDeclaration,
                        elements = listOf(convertElement(refactoring.movedAttribute))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
