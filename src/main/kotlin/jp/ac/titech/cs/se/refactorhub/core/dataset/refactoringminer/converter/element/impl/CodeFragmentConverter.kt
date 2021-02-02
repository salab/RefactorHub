package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.impl

import gr.uom.java.xmi.decomposition.AbstractCodeFragment
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.CodeElementConverter
import jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.CodeFragment

class CodeFragmentConverter : CodeElementConverter<AbstractCodeFragment> {
    override fun convert(element: AbstractCodeFragment): CodeFragment {
        return CodeFragment(
            location = convertLocation(element.locationInfo)
        )
    }
}
