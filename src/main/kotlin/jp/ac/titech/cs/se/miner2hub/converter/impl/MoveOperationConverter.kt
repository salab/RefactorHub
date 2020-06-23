package jp.ac.titech.cs.se.miner2hub.converter.impl

import gr.uom.java.xmi.diff.MoveOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.Converter
import jp.ac.titech.cs.se.miner2hub.converter.RefactoringOutput
import jp.ac.titech.cs.se.miner2hub.converter.convertCommit
import jp.ac.titech.cs.se.miner2hub.converter.element.convertElement
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.element.Element

class MoveOperationConverter : Converter<MoveOperationRefactoring> {
    override fun convert(refactoring: MoveOperationRefactoring, metadata: RefactoringMetadata): RefactoringOutput {
        refactoring.replacements.forEach {
            println("${it.type}: $it")
        }
        return RefactoringOutput(
            "MoveMethod",
            refactoring.toString(),
            convertCommit(metadata.commit),
            Refactoring.Data(
                mutableMapOf(
                    "target method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.originalOperation))
                    )
                ),
                mutableMapOf(
                    "moved method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.movedOperation))
                    )
                )
            )
        )
    }
}
