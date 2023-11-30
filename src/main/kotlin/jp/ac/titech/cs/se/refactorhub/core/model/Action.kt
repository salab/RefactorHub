package jp.ac.titech.cs.se.refactorhub.core.model

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.UUID

interface Action {
    val name: ActionName
    val type: ActionType
    val userId: UUID?
    val time: LocalDateTime
    val data: JsonNode
}

enum class ActionName {
    // Server
    UpdateChange,
    PutNewParameter,
    RemoveParameter,
    VerifyParameter,
    AppendParameterElement,
    UpdateParameterValue,
    ClearParameterValue,

    // Client
    OpenElementLocation,
    ToggleEditingElement,
    SetDisplayedFile
}

enum class ActionType {
    Client,
    Server
}
