package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring

import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import gr.uom.java.xmi.diff.MoveOperationRefactoring
import gr.uom.java.xmi.diff.RenameOperationRefactoring
import gr.uom.java.xmi.diff.RenameVariableRefactoring
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl.ExtractOperationConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl.MoveOperationConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl.RenameOperationConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.refactoring.impl.RenameVariableConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.oracle.RefOracleData
import org.refactoringminer.api.Refactoring

interface RefactoringConverter<T : Refactoring> {
    fun convert(refactoring: T, data: RefOracleData): jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring
}

fun convert(
    refactoring: Refactoring,
    data: RefOracleData
): jp.ac.titech.cs.se.refactorhub.tool.model.refactoring.Refactoring = when (refactoring) {
    is ExtractOperationRefactoring -> ExtractOperationConverter().convert(refactoring, data)
    is RenameOperationRefactoring -> RenameOperationConverter().convert(refactoring, data)
    is MoveOperationRefactoring -> MoveOperationConverter().convert(refactoring, data)
    is RenameVariableRefactoring -> RenameVariableConverter().convert(refactoring, data)
    else -> throw RuntimeException("Converter for ${refactoring::class.simpleName} is not implemented.")
}
