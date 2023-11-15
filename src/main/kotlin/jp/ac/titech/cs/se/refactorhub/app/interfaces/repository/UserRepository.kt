package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.User
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findBySubId(subId: Int): User?

    fun create(subId: Int, name: String): User
}
