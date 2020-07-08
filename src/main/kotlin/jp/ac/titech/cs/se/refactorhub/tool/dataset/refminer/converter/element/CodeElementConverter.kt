package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element

import gr.uom.java.xmi.LocationInfo
import gr.uom.java.xmi.UMLOperation
import gr.uom.java.xmi.decomposition.AbstractCodeFragment
import gr.uom.java.xmi.decomposition.OperationInvocation
import gr.uom.java.xmi.decomposition.VariableDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl.CodeFragmentConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl.OperationInvocationConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl.UMLOperationConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl.VariableDeclarationConverter
import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Range

interface CodeElementConverter<T> {
    fun convert(element: T): CodeElement
}

fun convertElement(element: Any): CodeElement = when (element) {
    is UMLOperation -> UMLOperationConverter().convert(element)
    is OperationInvocation -> OperationInvocationConverter().convert(element)
    is AbstractCodeFragment -> CodeFragmentConverter().convert(element)
    is VariableDeclaration -> VariableDeclarationConverter().convert(element)
    else -> throw RuntimeException("${element::class.simpleName} is not implemented.")
}

fun convertLocation(locationInfo: LocationInfo): Location {
    val range = locationInfo.codeRange()
    return Location(
        locationInfo.filePath,
        Range(
            range.startLine,
            range.startColumn,
            range.endLine,
            range.endColumn
        )
    )
}
