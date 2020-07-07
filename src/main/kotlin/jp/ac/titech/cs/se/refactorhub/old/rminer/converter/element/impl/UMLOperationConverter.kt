package jp.ac.titech.cs.se.refactorhub.old.rminer.converter.element.impl

import gr.uom.java.xmi.UMLOperation
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.element.ElementConverter
import jp.ac.titech.cs.se.refactorhub.old.rminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.old.models.element.impl.MethodDeclaration

class UMLOperationConverter : ElementConverter<UMLOperation> {
    override fun convert(element: UMLOperation): MethodDeclaration {
        return MethodDeclaration(
            element.name,
            element.className,
            convertLocation(element.locationInfo)
        )
    }
}
