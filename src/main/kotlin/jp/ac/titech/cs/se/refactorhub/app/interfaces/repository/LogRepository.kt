package jp.ac.titech.cs.se.refactorhub.app.interfaces.repository

import jp.ac.titech.cs.se.refactorhub.app.model.Log

interface LogRepository {
    fun save(log: Log): Log
}
