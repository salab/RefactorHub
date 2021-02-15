package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao

interface ModelConverter<T> {
    fun asModel(): T
}
