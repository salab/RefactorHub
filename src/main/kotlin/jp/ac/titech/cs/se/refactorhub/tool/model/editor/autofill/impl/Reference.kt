package jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.Autofill
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.AutofillType
import jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.Follow

@JsonDeserialize(`as` = Reference::class)
data class Reference(
    override val follows: List<Follow>
) : Autofill {
    override val type: AutofillType get() = AutofillType.Reference
}
