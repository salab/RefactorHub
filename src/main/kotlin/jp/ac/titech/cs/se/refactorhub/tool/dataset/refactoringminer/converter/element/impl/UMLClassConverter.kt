package jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.element.impl

import gr.uom.java.xmi.UMLClass
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ClassDeclaration

class UMLClassConverter : CodeElementConverter<UMLClass> {
    override fun convert(element: UMLClass): ClassDeclaration {
        return ClassDeclaration(
            element.name,
            convertLocation(element.locationInfo)
        )
    }
}
