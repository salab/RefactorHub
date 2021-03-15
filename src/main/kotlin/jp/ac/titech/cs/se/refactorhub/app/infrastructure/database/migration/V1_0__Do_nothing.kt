package jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("ClassName")
class V1_0__Do_nothing : BaseJavaMigration() {
    override fun migrate(context: Context) {
        transaction {
            //
        }
    }
}
