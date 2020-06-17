package jp.ac.titech.cs.se.miner2hub.converter.element.impl

import gr.uom.java.xmi.decomposition.OperationInvocation
import jp.ac.titech.cs.se.miner2hub.converter.element.ElementConverter
import jp.ac.titech.cs.se.miner2hub.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.models.element.impl.MethodInvocation

class OperationInvocationConverter : ElementConverter<OperationInvocation> {
    override fun convert(element: OperationInvocation): MethodInvocation {
        return MethodInvocation(
            element.methodName,
            "TODO",
            convertLocation(element.locationInfo)
        )
    }
}
