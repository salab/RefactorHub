package jp.ac.titech.cs.se.miner2hub.converter.impl

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.Converter
import jp.ac.titech.cs.se.miner2hub.converter.RefactoringOutput
import jp.ac.titech.cs.se.miner2hub.converter.convertCommit
import jp.ac.titech.cs.se.miner2hub.converter.element.convertElement
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.element.Element

class ExtractOperationConverter : Converter<ExtractOperationRefactoring> {
    override fun convert(refactoring: ExtractOperationRefactoring, metadata: RefactoringMetadata): RefactoringOutput {
        return RefactoringOutput(
            "ExtractMethod",
            refactoring.toString(),
            convertCommit(metadata.commit),
            Refactoring.Data(
                mutableMapOf(
                    "target method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        multiple = true,
                        elements = mutableListOf(convertElement(refactoring.sourceOperationBeforeExtraction))
                    ),
                    "extracted code" to Element.Data(
                        type = Element.Type.CodeFragments,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsFromSourceOperation.map { convertElement(it) }
                            .toMutableList()
                    )
                ),
                mutableMapOf(
                    "target method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        multiple = true,
                        elements = mutableListOf(convertElement(refactoring.sourceOperationAfterExtraction))
                    ),
                    "extracted method" to Element.Data(
                        type = Element.Type.MethodDeclaration,
                        required = true,
                        elements = mutableListOf(convertElement(refactoring.extractedOperation))
                    ),
                    "extracted method invocation" to Element.Data(
                        type = Element.Type.MethodInvocation,
                        required = true,
                        multiple = true,
                        elements = refactoring.extractedOperationInvocations.map { convertElement(it) }.toMutableList()
                    ),
                    "extracted code" to Element.Data(
                        type = Element.Type.CodeFragments,
                        multiple = true,
                        elements = refactoring.extractedCodeFragmentsToExtractedOperation.map { convertElement(it) }
                            .toMutableList()
                    )
                )
            )
        )
    }
}
