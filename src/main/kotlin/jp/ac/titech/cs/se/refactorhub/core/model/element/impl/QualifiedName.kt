package jp.ac.titech.cs.se.refactorhub.core.model.element.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementType
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location

@JsonDeserialize(`as` = QualifiedName::class)
data class QualifiedName(
    override val name: String? = null,
    override val location: Location? = null
) : Name {
    override val type: CodeElementType get() = CodeElementType.QualifiedName
}
