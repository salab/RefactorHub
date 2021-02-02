package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.impl

import gr.uom.java.xmi.UMLOperation
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodDeclaration

class UMLOperationConverter : CodeElementConverter<UMLOperation> {
    override fun convert(element: UMLOperation): MethodDeclaration {
        return MethodDeclaration(
            element.name,
            element.className,
            convertLocation(element.locationInfo)
        )
    }
}
