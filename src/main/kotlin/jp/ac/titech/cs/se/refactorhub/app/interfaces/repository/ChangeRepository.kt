package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Change
import java.util.UUID

interface ChangeRepository {
    fun findById(id: UUID): Change?

    fun create(
        snapshotId: UUID,
        orderIndex: Int,
        typeName: String,
        description: String,
        parameterData: Change.ParameterData
    ): Change

    fun updateById(
        id: UUID,
        orderIndex: Int? = null,
        typeName: String? = null,
        description: String? = null,
        parameterData: Change.ParameterData? = null
    ): Change

    fun deleteById(id: UUID)
}
