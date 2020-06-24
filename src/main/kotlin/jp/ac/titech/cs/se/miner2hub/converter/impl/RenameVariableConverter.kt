package jp.ac.titech.cs.se.miner2hub.converter.impl

import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.Converter
import jp.ac.titech.cs.se.miner2hub.converter.RefactoringOutput
import jp.ac.titech.cs.se.miner2hub.converter.convertCommit
import jp.ac.titech.cs.se.miner2hub.converter.element.convertElement
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.element.Element

class RenameVariableConverter : Converter<RenameVariableRefactoring> {
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
