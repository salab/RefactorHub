package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl

import gr.uom.java.xmi.UMLOperation
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodDeclaration

class UMLOperationConverter : CodeElementConverter<UMLOperation> {
    override fun convert(element: UMLOperation): MethodDeclaration {
        return MethodDeclaration(
            element.name,
            element.className,
            convertLocation(element.locationInfo)
        )
    }
}
