package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration

import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Actions
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.CommitContents
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Commits
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ExperimentRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Experiments
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDrafts
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringToRefactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringTypes
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Refactorings
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.Users
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class V1__Create_tables : BaseJavaMigration() {
    override fun migrate(context: Context) {
        transaction {
            createTables()
        }
    }
}

private fun createTables() {
    SchemaUtils.create(
        Commits,
        Users,
        Refactorings,
        RefactoringToRefactorings,
        RefactoringTypes,
        RefactoringDrafts,
        Experiments,
        ExperimentRefactorings,
        CommitContents,
        Actions
    )
}
