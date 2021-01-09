package jp.ac.titech.cs.se.refactorhub.tool.model

import com.fasterxml.jackson.databind.JsonNode

interface Log {
    val event: LogEvent
    val type: LogType
    val user: Int
    val time: Long
    val data: JsonNode
}

enum class LogEvent {
    Save,
    Discard,
    Update,
    PutElementKey,
    RemoveElementKey,
    VerifyElement,
    AppendElementValue,
    UpdateElementValue,
    RemoveElementValue,
    Fork,
    Edit,

    OpenElementLocation,
    ToggleEditingElement,
    SetDisplayedFile
}

enum class LogType {
    Client,
    Server
}
