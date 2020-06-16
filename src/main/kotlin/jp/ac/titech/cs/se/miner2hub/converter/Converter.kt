package jp.ac.titech.cs.se.miner2hub.converter

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.impl.ExtractOperationConverter
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import org.refactoringminer.api.Refactoring

interface Converter<T : Refactoring> {
    fun convert(refactoring: T, metadata: RefactoringMetadata): RefactoringOutput
}

fun convert(refactoring: Refactoring, metadata: RefactoringMetadata): RefactoringOutput = when (refactoring) {
    is ExtractOperationRefactoring -> ExtractOperationConverter().convert(refactoring, metadata)
    else -> throw RuntimeException("${refactoring::class.simpleName} is not implemented.")
}