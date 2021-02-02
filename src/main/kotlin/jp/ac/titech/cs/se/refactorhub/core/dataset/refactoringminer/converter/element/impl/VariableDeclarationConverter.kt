package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.impl

import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ParameterDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration

class VariableDeclarationConverter : CodeElementConverter<gr.uom.java.xmi.decomposition.VariableDeclaration> {
    override fun convert(element: gr.uom.java.xmi.decomposition.VariableDeclaration): CodeElement {
        // TODO: Auto complete method/class name
        return if (element.isParameter) ParameterDeclaration(
            element.variableName,
            location = convertLocation(element.locationInfo)
        ) else VariableDeclaration(
            element.variableName,
            location = convertLocation(element.locationInfo)
        )
    }
}
