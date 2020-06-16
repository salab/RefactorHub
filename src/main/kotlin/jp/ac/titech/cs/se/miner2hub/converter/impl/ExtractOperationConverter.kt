package jp.ac.titech.cs.se.miner2hub.converter.impl

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.Converter
import jp.ac.titech.cs.se.miner2hub.converter.RefactoringOutput
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata

class ExtractOperationConverter : Converter<ExtractOperationRefactoring> {
    override fun convert(refactoring: ExtractOperationRefactoring, metadata: RefactoringMetadata): RefactoringOutput {
        return RefactoringOutput(
            "ExtractMethod",
            refactoring.toString(),
            metadata.commit,
            RefactoringOutput.Data(
                mapOf(),
                mapOf()
            )
        )
    }
}