package jp.ac.titech.cs.se.refactorhub.controllers.draft.requests

data class AddElementKeyRequest(
    val key: String,
    val type: String,
    val multiple: Boolean
)