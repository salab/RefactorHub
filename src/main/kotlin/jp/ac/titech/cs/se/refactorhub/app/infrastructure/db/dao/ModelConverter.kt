package jp.ac.titech.cs.se.refactorhub.app.infrastructure.db.dao

interface ModelConverter<T> {
    fun asModel(): T
}
