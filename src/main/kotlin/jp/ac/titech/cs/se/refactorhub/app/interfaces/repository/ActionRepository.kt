package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Action

interface ActionRepository {
    fun save(log: Action): Action
}
