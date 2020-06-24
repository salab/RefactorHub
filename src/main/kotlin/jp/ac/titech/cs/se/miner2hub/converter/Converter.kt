package jp.ac.titech.cs.se.miner2hub.converter

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import gr.uom.java.xmi.diff.MoveOperationRefactoring
import gr.uom.java.xmi.diff.RenameOperationRefactoring
import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.miner2hub.converter.impl.ExtractOperationConverter
import jp.ac.titech.cs.se.miner2hub.converter.impl.MoveOperationConverter
import jp.ac.titech.cs.se.miner2hub.converter.impl.RenameOperationConverter
import jp.ac.titech.cs.se.miner2hub.converter.impl.RenameVariableConverter
import jp.ac.titech.cs.se.miner2hub.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Commit
import org.refactoringminer.api.Refactoring

interface Converter<T : Refactoring> {
    fun convert(refactoring: T, metadata: RefactoringMetadata): RefactoringOutput
}

fun convert(refactoring: Refactoring, metadata: RefactoringMetadata): RefactoringOutput = when (refactoring) {
    is ExtractOperationRefactoring -> ExtractOperationConverter().convert(refactoring, metadata)
    is RenameOperationRefactoring -> RenameOperationConverter().convert(refactoring, metadata)
    is MoveOperationRefactoring -> MoveOperationConverter().convert(refactoring, metadata)
    is RenameVariableRefactoring -> RenameVariableConverter().convert(refactoring, metadata)
    else -> throw RuntimeException("${refactoring::class.simpleName} is not implemented.")
}

fun convertCommit(commit: RefactoringMetadata.Commit) = Commit(commit.sha, commit.owner, commit.repo)
