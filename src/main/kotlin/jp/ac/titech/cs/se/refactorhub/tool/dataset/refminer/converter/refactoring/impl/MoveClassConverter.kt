package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.MoveClassRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElementType

class MoveClassConverter :
    RefactoringConverter<MoveClassRefactoring> {
    override fun convert(
        refactoring: MoveClassRefactoring,
        data: RefOracleData
    ): Refactoring {
        return Refactoring(
            refactoring.refactoringType.displayName,
            data.commit,
            Refactoring.Data(
                mapOf(
                    "target class" to CodeElementHolder(
                        type = CodeElementType.ClassDeclaration,
                        elements = listOf(convertElement(refactoring.originalClass))
                    )
                ),
                mapOf(
                    "moved class" to CodeElementHolder(
                        type = CodeElementType.ClassDeclaration,
                        elements = listOf(convertElement(refactoring.movedClass))
                    )
                )
            ),
            refactoring.toString()
        )
    }
}
