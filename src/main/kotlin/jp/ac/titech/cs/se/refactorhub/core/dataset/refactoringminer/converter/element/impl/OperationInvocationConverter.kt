package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.impl

import gr.uom.java.xmi.decomposition.OperationInvocation
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodInvocation

class OperationInvocationConverter : CodeElementConverter<OperationInvocation> {
    override fun convert(element: OperationInvocation): MethodInvocation {
        return MethodInvocation(
            methodName = element.methodName,
            location = convertLocation(element.locationInfo)
        )
    }
}
