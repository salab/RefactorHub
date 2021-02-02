package jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.Autofill
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.AutofillType
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.Follow
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

@JsonDeserialize(`as` = Surround::class)
data class Surround(
    val element: CodeElementType,
    override val follows: List<Follow>
) : Autofill {
    override val type: AutofillType get() = AutofillType.Surround
}
