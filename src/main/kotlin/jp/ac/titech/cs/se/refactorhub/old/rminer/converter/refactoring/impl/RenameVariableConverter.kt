package jp.ac.titech.cs.se.refactorhub.old.rminer.converter.refactoring.impl

import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.refactoring.RefactoringConverter
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.refactoring.RefactoringOutput
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.refactoring.convertCommit
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.element.convertElement
import jp.ac.titech.cs.se.refactorhub.old.rminer.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.old.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.old.models.element.Element

class RenameVariableConverter :
    RefactoringConverter<RenameVariableRefactoring> {
    override fun convert(refactoring: RenameVariableRefactoring, metadata: RefactoringMetadata): RefactoringOutput {
        refactoring.variableReferences.forEach {
            println(it)
        }
        return RefactoringOutput(
            refactoring.refactoringType.displayName,
            refactoring.toString(),
            convertCommit(metadata.commit),
            Refactoring.Data(
                mutableMapOf(
                    "target method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.operationBefore))
                    ),
                    "target variable" to Element.Data(
                        type = Element.Type.VariableDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.originalVariable))
                    )
                ),
                mutableMapOf(
                    "target method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.operationAfter))
                    ),
                    "renamed variable" to Element.Data(
                        type = Element.Type.VariableDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.renamedVariable))
                    )
                )
            )
        )
    }
}
