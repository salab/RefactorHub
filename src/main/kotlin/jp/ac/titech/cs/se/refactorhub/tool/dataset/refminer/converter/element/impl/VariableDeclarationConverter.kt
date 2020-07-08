package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl

import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableDeclaration

class VariableDeclarationConverter : CodeElementConverter<gr.uom.java.xmi.decomposition.VariableDeclaration> {
    override fun convert(element: gr.uom.java.xmi.decomposition.VariableDeclaration): VariableDeclaration {
        return VariableDeclaration(
            element.variableName,
            location = convertLocation(element.locationInfo)
        )
    }
}
