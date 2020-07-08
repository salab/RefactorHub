package jp.ac.titech.cs.se.refactorhub.app.infrastructure.auth

data class Session(val id: Int, val accessToken: String) {
    companion object {
        const val KEY = "app_session"
    }
}
