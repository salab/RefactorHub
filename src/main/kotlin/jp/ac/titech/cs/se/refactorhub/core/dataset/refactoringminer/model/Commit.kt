package jp.ac.titech.cs.se.refactorhub.core.dataset.refactoringminer.model

import jp.ac.titech.cs.se.refactorhub.core.model.Commit

data class Commit(
    override val sha: String,
    override var owner: String,
    override var repository: String
) : Commit
