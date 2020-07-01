package jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.impl

import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.ElementConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.models.element.impl.VariableDeclaration

class VariableDeclarationConverter : ElementConverter<gr.uom.java.xmi.decomposition.VariableDeclaration> {
    override fun convert(element: gr.uom.java.xmi.decomposition.VariableDeclaration): VariableDeclaration {
        return VariableDeclaration(
            element.variableName,
            "TODO",
            "TODO",
            convertLocation(element.locationInfo)
        )
    }
}
