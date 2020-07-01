package jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.refactoring

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import gr.uom.java.xmi.diff.MoveOperationRefactoring
import gr.uom.java.xmi.diff.RenameOperationRefactoring
import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.refactoring.impl.ExtractOperationConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.refactoring.impl.MoveOperationConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.refactoring.impl.RenameOperationConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.refactoring.impl.RenameVariableConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.oracle.RefactoringMetadata
import jp.ac.titech.cs.se.refactorhub.models.Commit
import org.refactoringminer.api.Refactoring

interface RefactoringConverter<T : Refactoring> {
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
