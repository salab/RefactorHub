package jp.ac.titech.cs.se.refactorhub.config.filter.log

import java.util.Date

data class RequestLog(
    val user: String,
    val date: Date,
    val method: String,
    val path: String,
    val query: String,
    val content: String
)
