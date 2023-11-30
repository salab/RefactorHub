package jp.ac.titech.cs.se.refactorhub.app.model

import com.fasterxml.jackson.databind.JsonNode
import jp.ac.titech.cs.se.refactorhub.core.model.Action
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.ActionType
import java.time.LocalDateTime
import java.util.UUID

data class Action(
    override val name: ActionName,
    override val type: ActionType,
    override val userId: UUID,
    override val time: LocalDateTime,
    override val data: JsonNode
) : Action
