package jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth

import jp.ac.titech.cs.se.refactorhub.app.exception.BadRequestException
import java.lang.IllegalArgumentException
import java.util.UUID

data class Session(val userId: String, val accessToken: String) {
    companion object {
        const val KEY = "app_session"
    }
}

fun String.toUUID(): UUID {
    try {
        return UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw BadRequestException("\"$this\" is not uuid")
    }
}
