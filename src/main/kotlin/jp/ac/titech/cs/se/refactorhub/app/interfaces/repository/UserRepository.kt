package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.User

interface UserRepository {
    fun findById(id: Int): User?
    fun findBySubId(subId: Int): User?
    fun create(subId: Int, name: String): User
}
