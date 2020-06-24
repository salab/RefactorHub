package jp.ac.titech.cs.se.miner2hub.converter.impl

import gr.uom.java.xmi.diff.RenameOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.Converter
import jp.ac.titech.cs.se.miner2hub.converter.RefactoringOutput
import jp.ac.titech.cs.se.miner2hub.converter.convertCommit
import jp.ac.titech.cs.se.miner2hub.converter.element.convertElement
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.element.Element

class RenameOperationConverter : Converter<RenameOperationRefactoring> {
    override fun convert(refactoring: RenameOperationRefactoring, metadata: RefactoringMetadata): RefactoringOutput {
        return RefactoringOutput(
            refactoring.refactoringType.displayName,
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
                    "renamed method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.renamedOperation))
                    )
                )
            )
        )
    }
}
