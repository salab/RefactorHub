package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.impl

import gr.uom.java.xmi.decomposition.AbstractCodeFragment
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.CodeFragment

class CodeFragmentConverter : CodeElementConverter<AbstractCodeFragment> {
    override fun convert(element: AbstractCodeFragment): CodeFragment {
        return CodeFragment(
            location = convertLocation(element.locationInfo)
        )
    }
}
