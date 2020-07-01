package jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.impl

import gr.uom.java.xmi.decomposition.AbstractCodeFragment
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.ElementConverter
import jp.ac.titech.cs.se.refactorhub.dataset.rminer.converter.element.convertLocation
import jp.ac.titech.cs.se.refactorhub.models.element.impl.CodeFragments

class CodeFragmentConverter : ElementConverter<AbstractCodeFragment> {
    override fun convert(element: AbstractCodeFragment): CodeFragments {
        return CodeFragments(
            "TODO",
            "TODO",
            convertLocation(element.locationInfo)
        )
    }
}
