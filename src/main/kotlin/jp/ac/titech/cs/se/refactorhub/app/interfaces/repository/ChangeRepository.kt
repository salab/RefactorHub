package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Change
import java.util.UUID

interface ChangeRepository {
    fun findById(id: UUID): Change?

    fun create(
        typeName: String,
        description: String,
        parameterData: Change.ParameterData
    ): Change

    fun updateById(
        id: UUID,
        typeName: String? = null,
        description: String? = null,
        parameterData: Change.ParameterData? = null
    ): Change

    fun deleteById(id: UUID)
}
