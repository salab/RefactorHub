package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.impl

import gr.uom.java.xmi.UMLAttribute
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration

class UMLAttributeConverter : CodeElementConverter<UMLAttribute> {
    override fun convert(element: UMLAttribute): FieldDeclaration {
        return FieldDeclaration(
            element.name,
            element.className,
            convertLocation(element.locationInfo)
        )
    }
}
