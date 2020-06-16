package jp.ac.titech.cs.se.miner2hub.converter

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.impl.ExtractOperationConverter
import org.refactoringminer.api.Refactoring

interface Converter<T : Refactoring> {
    fun convert(refactoring: T): RefactoringOutput
}

fun convert(refactoring: Refactoring): RefactoringOutput = when (refactoring) {
    is ExtractOperationRefactoring -> ExtractOperationConverter().convert(refactoring)
    else -> throw RuntimeException("${refactoring::class.simpleName} is not implemented.")
}