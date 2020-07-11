package jp.ac.titech.cs.se.refactorhub.tool.dataset.refminer.model

import jp.ac.titech.cs.se.refactorhub.tool.model.Commit

data class Commit(
    override val sha: String,
    override var owner: String,
    override var repository: String
) : Commit
