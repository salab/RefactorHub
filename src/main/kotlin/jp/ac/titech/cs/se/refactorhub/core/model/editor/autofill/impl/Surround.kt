package jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.Autofill
import jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.AutofillType
import jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.Follow
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType

@JsonDeserialize(`as` = Surround::class)
data class Surround(
    val element: CodeElementType,
    override val follows: List<Follow>
) : Autofill {
    override val type: AutofillType get() = AutofillType.Surround
}
