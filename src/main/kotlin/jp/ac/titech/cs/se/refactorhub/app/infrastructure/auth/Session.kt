package jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth

import java.util.UUID

data class Session(val id: UUID, val accessToken: String) {
    companion object {
        const val KEY = "app_session"
    }
}
