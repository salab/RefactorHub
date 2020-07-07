package jp.ac.titech.cs.se.refactorhub.old.controllers.draft.requests

data class AddElementKeyRequest(
    val key: String,
    val type: String,
    val multiple: Boolean
)
