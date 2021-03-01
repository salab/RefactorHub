package jp.ac.titech.cs.se.refactorhub.core.analysis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.ActionDao
import jp.ac.titech.cs.se.refactorhub.app.infrastructure.database.dao.RefactoringDao
import jp.ac.titech.cs.se.refactorhub.app.model.Action
import jp.ac.titech.cs.se.refactorhub.app.model.Refactoring
import jp.ac.titech.cs.se.refactorhub.core.model.ActionName
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementHolder
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val parser = ArgParser("analyzer")
    val url by parser.option(ArgType.String, "url", "u", "Database URL from which export actions and refactorings")

    parser.parse(args)

    url?.let { export(it) }

    outputAutofillWO()
    outputAutofillWOCodeElement()

    analyze()
}

private const val OUTPUT_PATH = "analysis"

private fun export(url: String) {
    startKoin {
        modules(
            module {
                single { jacksonObjectMapper() }
            }
        )
    }

    val uri = URI(url)
    val (username, password) = uri.userInfo.split(":")
    val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
    Database.connect(
        HikariDataSource(
            HikariConfig().apply {
                this.driverClassName = "org.postgresql.Driver"
                this.jdbcUrl = jdbcUrl
                this.username = username
                this.password = password
                validate()
            }
        )
    )

    val actions = getOutputFile("actions.ndjson")
    val refactorings = getOutputFile("refactorings.ndjson")
    transaction {
        ActionDao.all().map { it.asModel() }.sortedBy { it.time }.forEach {
            BufferedWriter(FileWriter(actions, true)).use { out ->
                out.appendLine(
                    jacksonObjectMapper().writeValueAsString(it)
                )
            }
        }
        RefactoringDao.all().map { it.asModel() }.sortedBy { it.id }.forEach {
            BufferedWriter(FileWriter(refactorings, true)).use { out ->
                out.appendLine(
                    jacksonObjectMapper().writeValueAsString(it)
                )
            }
        }
    }
}

private fun getOutputFile(name: String): File {
    val file = File("$OUTPUT_PATH/$name")
    file.parentFile.mkdirs()
    file.delete()
    file.createNewFile()
    return file
}

// Constants
private val USERS = listOf("User 1", "User 2", "User 3", "User 4")
private val USER_IDS = listOf(6, 5, 7, 4)
private val TASK_SIZE = 4
private val WO_AUTOFILL = listOf(
    listOf(1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1),
    listOf(1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1),
    listOf(0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0),
    listOf(0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0)
)
private val EXP1_TABLE = listOf(
    listOf(38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53),
    listOf(74, 75, 76, 77, 66, 67, 68, 69, 78, 79, 80, 81, 70, 71, 72, 73),
    listOf(98, 99, 100, 101, 106, 107, 108, 109, 94, 95, 96, 97, 102, 103, 104, 105),
    listOf(134, 135, 136, 137, 130, 131, 132, 133, 126, 127, 128, 129, 122, 123, 124, 125)
)
private val EXP2_TABLE = listOf(
    listOf(54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65),
    listOf(88, 89, 86, 87, 92, 93, 90, 91, 82, 83, 84, 85),
    listOf(118, 119, 120, 121, 110, 111, 112, 113, 116, 117, 114, 115),
    listOf(140, 141, 138, 139, 144, 145, 142, 143, 148, 149, 146, 147)
)
private val EXP1_REFACTORING_TYPES =
    jacksonObjectMapper().readValue<List<RefactoringType>>(object {}.javaClass.classLoader.getResourceAsStream("types/experiment.json")!!)
private val EXP2_REFACTORING_TYPES = listOf(
    RefactoringType("Extract Variable"),
    RefactoringType("Move Declaration"),
    RefactoringType("Replace Expression With Variable")
)

private data class RefactoringType(
    override val name: String,
    override val before: Map<String, CodeElementMetadata> = mapOf(),
    override val after: Map<String, CodeElementMetadata> = mapOf(),
    override val description: String = "",
    override val url: String = ""
) : jp.ac.titech.cs.se.refactorhub.core.model.refactoring.RefactoringType

private fun writeCsv(csv: File, list: List<Any>) {
    BufferedWriter(FileWriter(csv, true)).use { out ->
        out.appendLine(list.joinToString("\",\"", "\"", "\""))
    }
}

private fun Map<String, CodeElementMetadata>.sorted(): Map<String, CodeElementMetadata> {
    return this.toList().sortedWith { a, b ->
        if (a.second.required && !b.second.required) -1
        else if (!a.second.required && b.second.required) 1
        else a.first.compareTo(b.first)
    }.toMap()
}

private fun outputAutofillWO() {
    val csv = getOutputFile("wo-autofill.csv")
    writeCsv(csv, listOf("Task", "Type") + USERS)
    WO_AUTOFILL[0].indices.forEach { i ->
        val type = EXP1_REFACTORING_TYPES[i / TASK_SIZE]
        writeCsv(csv, listOf(i + 1, type.name) + WO_AUTOFILL.map { it[i] == 1 })
    }
}

private fun outputAutofillWOCodeElement() {
    val csv = getOutputFile("wo-autofill-element.csv")
    writeCsv(csv, listOf("Task", "Type", "Category", "Element") + USERS)
    WO_AUTOFILL[0].indices.forEach { i ->
        val type = EXP1_REFACTORING_TYPES[i / TASK_SIZE]
        type.before.sorted().entries.forEach {
            writeCsv(
                csv,
                listOf(i + 1, type.name, "before", it.key) +
                    WO_AUTOFILL.map { s -> s[i] == 1 && it.value.autofills.isNotEmpty() }
            )
        }
        type.after.sorted().entries.forEach {
            writeCsv(
                csv,
                listOf(i + 1, type.name, "after", it.key) +
                    WO_AUTOFILL.map { s -> s[i] == 1 && it.value.autofills.isNotEmpty() }
            )
        }
    }
}

private fun analyze() {
    val mapper = jacksonObjectMapper()
    val actions = Files.readAllLines(Paths.get("$OUTPUT_PATH/actions.ndjson")).filter { it.isNotBlank() }
        .map { mapper.readValue<Action>(it) }
    val refactorings = Files.readAllLines(Paths.get("$OUTPUT_PATH/refactorings.ndjson")).filter { it.isNotBlank() }
        .map { mapper.readValue<Refactoring>(it) }

    val exp1Results = TaskResult.getTaskResults(actions, refactorings, EXP1_TABLE, EXP1_REFACTORING_TYPES)
    val exp2Results = TaskResult.getTaskResults(actions, refactorings, EXP2_TABLE, EXP2_REFACTORING_TYPES)

    analyzeTimeSpent(exp1Results)
    analyzeAgreement(exp1Results, 1)
    analyzeAgreement(exp2Results, 2)
    analyzeExp2Agreement(exp2Results)
}

private fun analyzeTimeSpent(results: List<TaskResult>) {
    analyzeTimeSpentByTask(results)
    analyzeTimeSpentByCodeElement(results, false)
    analyzeTimeSpentByCodeElement(results, true)
}

private fun analyzeTimeSpentByTask(results: List<TaskResult>) {
    val csv1 = getOutputFile("time-spent.csv")
    writeCsv(csv1, listOf("Task", "Type") + USERS)
    results.withIndex().forEach { (i, result) ->
        writeCsv(csv1, listOf("${i + 1}", result.type.name) + result.users.map { calcTimeSpent(it.actions) })
    }

    val csv2 = getOutputFile("time-spent-start.csv")
    writeCsv(csv2, listOf("Task", "Type") + USERS)
    results.withIndex().forEach { (i, result) ->
        writeCsv(csv2, listOf("${i + 1}", result.type.name) + result.users.map { calcTimeSpentFromStart(it.actions) })
    }
}

private fun analyzeTimeSpentByCodeElement(results: List<TaskResult>, fix: Boolean) {
    val csv = getOutputFile("time-spent-element${if (fix) "-fix" else ""}.csv")
    writeCsv(csv, listOf("Task", "Type", "Category", "Element") + USERS)
    results.withIndex().forEach { (i, result) ->
        val userTimes = result.users.map {
            calcTimeSpentByCodeElement(it.actions, result.type, fix)
        }
        result.type.before.sorted().keys.forEach {
            writeCsv(
                csv,
                listOf(i + 1, result.type.name, "before", it) + userTimes.map { times -> times.before[it] ?: -1 }
            )
        }
        result.type.after.sorted().keys.forEach {
            writeCsv(
                csv,
                listOf(i + 1, result.type.name, "after", it) + userTimes.map { times -> times.after[it] ?: -1 }
            )
        }
    }
}

private fun calcTimeSpent(actions: List<Action>): Long {
    return actions.last().time - actions.first().time
}

private fun calcTimeSpentFromStart(actions: List<Action>): Long {
    return actions.last().time - actions.first { it.name == ActionName.ToggleEditingElement }.time
}

private fun calcTimeSpentByCodeElement(
    actions: List<Action>,
    type: RefactoringType,
    fix: Boolean
): TimeSpentByCodeElement {
    var last = actions.first()
    val times = TimeSpentByCodeElement(
        type.before.keys.associateWith { 0L }.toMutableMap(),
        type.after.keys.associateWith { 0L }.toMutableMap()
    )
    actions.forEach { action ->
        if (action.name == ActionName.UpdateCodeElementValue || action.name == ActionName.VerifyCodeElementHolder) {
            if (action.data["category"].asText() == "before") {
                times.before[action.data["key"].asText()] =
                    (times.before[action.data["key"].asText()] ?: 0) + (action.time - last.time)
            } else {
                times.after[action.data["key"].asText()] =
                    (times.after[action.data["key"].asText()] ?: 0) + (action.time - last.time)
            }
            last = action
        }
    }
    if (fix) {
        // Not Updated/Verified -> Last Update - Save
        val time = actions.last().time - last.time
        type.before.keys.filter { times.before[it] == 0L }
            .let { it.forEach { key -> times.before[key] = time / it.size } }
        type.after.keys.filter { times.after[it] == 0L }.let { it.forEach { key -> times.after[key] = time / it.size } }
    }
    return times
}

private data class TaskResult(
    val users: List<User>,
    val type: RefactoringType
) {
    data class User(
        val actions: List<Action>,
        val refactoring: Refactoring
    )

    companion object {
        fun getTaskResults(
            actions: List<Action>,
            refactorings: List<Refactoring>,
            table: List<List<Int>>,
            types: List<RefactoringType>
        ): List<TaskResult> {
            return table[0].indices.map { i ->
                val users = USER_IDS.withIndex().map { (j, id) ->
                    val userActions = actions.filter { action -> action.user == id }
                    val start = userActions.indexOfFirst { action ->
                        action.name == ActionName.Fork && action.data.get("id").asInt() == table[j][i]
                    }
                    val end = start + userActions.subList(start, userActions.size)
                        .indexOfFirst { action -> action.name == ActionName.Save }
                    val refactoring = refactorings.first { it.parentId == table[j][i] }
                    User(userActions.subList(start, end + 1), refactoring)
                }
                TaskResult(users, types[i / TASK_SIZE])
            }
        }
    }
}

private data class TimeSpentByCodeElement(
    val before: MutableMap<String, Long>,
    val after: MutableMap<String, Long>
)

//

private fun analyzeAgreement(results: List<TaskResult>, exp: Int) {
    val keyCsv = getOutputFile("agreement-key-$exp.csv")
    val valCsv = getOutputFile("agreement-val-$exp.csv")
    writeCsv(keyCsv, listOf("Task", "Type") + USERS)
    // Agreement by element names
    calcTaskGroups(results, ::equalsKey).let {
        it.withIndex().forEach { (i, users) -> writeCsv(keyCsv, listOf(i + 1, results[i].type.name) + users) }
    }
    // Agreement by element names
    calcTaskGroups(results, ::equalsValue).let {
        it.withIndex().forEach { (i, users) -> writeCsv(valCsv, listOf(i + 1, results[i].type.name) + users) }
    }
}

private fun analyzeExp2Agreement(results: List<TaskResult>) {
    val csv = getOutputFile("agreement-element-2.csv")
    writeCsv(csv, listOf("Task", "Type", "Category", "Element", "Count", "Names"))
    results.withIndex().forEach { (i, result) ->
        val before = mutableListOf<MutableList<Map.Entry<String, CodeElementHolder>>>()
        val after = mutableListOf<MutableList<Map.Entry<String, CodeElementHolder>>>()
        result.users.forEach { user ->
            user.refactoring.data.before.entries.forEach { entry ->
                val group = before.find { g ->
                    equalsCodeElements(g.first().value.elements, entry.value.elements)
                }
                if (group != null) group.add(entry)
                else before.add(mutableListOf(entry))
            }
            user.refactoring.data.after.entries.forEach { entry ->
                val group = after.find { g ->
                    equalsCodeElements(g.first().value.elements, entry.value.elements)
                }
                if (group != null) group.add(entry)
                else after.add(mutableListOf(entry))
            }
        }

        before.forEach { entries ->
            writeCsv(
                csv,
                listOf(
                    i + 1,
                    result.type.name,
                    "before",
                    entries.first().value.type,
                    entries.size,
                    entries.map { it.key }
                )
            )
        }
        after.forEach { entries ->
            writeCsv(
                csv,
                listOf(
                    i + 1,
                    result.type.name,
                    "after",
                    entries.first().value.type,
                    entries.size,
                    entries.map { it.key }
                )
            )
        }
    }
}

private fun calcTaskGroups(
    results: List<TaskResult>,
    equals: (a: Refactoring, b: Refactoring) -> Boolean
): List<List<Int>> {
    return results.map { result ->
        reGroup(
            result.users.fold(
                listOf(),
                { group, user ->
                    group + listOf(
                        group.withIndex().find { equals(user.refactoring, result.users[it.index].refactoring) }?.value
                            ?: ((group.maxOrNull() ?: -1) + 1)
                    )
                }
            )
        )
    }
}

private fun equalsKey(a: Refactoring, b: Refactoring): Boolean {
    return a.data.before.keys == b.data.before.keys && a.data.after.keys == b.data.after.keys
}

private fun equalsValue(a: Refactoring, b: Refactoring): Boolean {
    return a.data.before.entries.all {
        b.data.before[it.key]?.elements?.let { elements ->
            equalsCodeElements(it.value.elements, elements)
        } ?: false
    } &&
        a.data.after.entries.all {
            b.data.after[it.key]?.elements?.let { elements ->
                equalsCodeElements(it.value.elements, elements)
            } ?: false
        }
}

private fun equalsCodeElements(a: List<CodeElement>, b: List<CodeElement>): Boolean {
    return a.all { element ->
        b.any { it == element }
    } && b.all { element ->
        a.any { it == element }
    }
}

private fun reGroup(group: List<Int>): List<Int> {
    val map = group.toSet().map { i -> i to group.count { it == i } }.sortedBy { -it.second }.map { it.first }
    return group.map { map[it] }
}
