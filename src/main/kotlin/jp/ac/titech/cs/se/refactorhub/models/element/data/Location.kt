package jp.ac.titech.cs.se.refactorhub.models.element.data

import java.io.Serializable

data class Location(
    var path: String = "",
    var range: Range = Range()
) : Serializable
