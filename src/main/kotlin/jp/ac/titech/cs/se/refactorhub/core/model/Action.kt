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
    // annotations
    GetAnnotation,
    PublishAnnotation,

    // snapshots
    AppendTemporarySnapshot,
    ModifyTemporarySnapshot,
    SettleTemporarySnapshot,

    // changes
    AppendChange,
    UpdateChange,
    RemoveChange,

    // parameters
    PutNewParameter,
    AppendParameterElement,
    RemoveParameter,
    VerifyParameter,
    UpdateParameterValue,
    ClearParameterValue,

    // Client
    // routing
    OpenExperiments,
    OpenExperiment,
    StartAnnotation,
    OpenAnnotation,

    // viewer
    UpdateViewers,
    ChangeMainViewer,

    // changes
    FilterChangeType,
    SwitchCurrentChange,
    OpenChangeInformation,

    // parameters
    OpenElementLocation,
    ToggleEditingElement,
    HoverElement,
    ToggleOpeningParameterList,

    // common token sequences
    ChangeCommonTokenSequencesSetting,
    HoverCommonTokenSequences,
    SearchCommonTokenSequences,
    CloseCommonTokenSequencesSearchResult,
    ShowPreviousCommonTokenSequence,
    ShowNextCommonTokenSequence,
}

enum class ActionType {
    Client,
    Server
}
