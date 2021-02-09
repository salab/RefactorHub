package jp.ac.titech.cs.se.refactorhub.core.model

import com.fasterxml.jackson.databind.JsonNode

interface Action {
    val name: ActionName
    val type: ActionType
    val user: Int
    val time: Long
    val data: JsonNode
}

enum class ActionName {
    // Server
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

    // Client
    OpenElementLocation,
    ToggleEditingElement,
    SetDisplayedFile
}

enum class ActionType {
    Client,
    Server
}
