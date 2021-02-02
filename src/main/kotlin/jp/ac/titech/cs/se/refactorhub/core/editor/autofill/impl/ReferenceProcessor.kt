package jp.ac.titech.cs.se.refactorhub.core.editor.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.editor.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.editor.CommitFileContents
import jp.ac.titech.cs.se.refactorhub.core.model.editor.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Name
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration
import java.util.regex.Pattern

class ReferenceProcessor : AutofillProcessor<Reference> {
    override fun process(
        autofill: Reference,
        follow: CodeElement,
        category: DiffCategory,
        contents: CommitFileContents
    ): List<CodeElement> {
        val name = when (follow) {
            is ClassDeclaration -> follow.name
            is FieldDeclaration -> follow.name
            is VariableDeclaration -> follow.name
            else -> return listOf() // TODO
        }
        // TODO
        return contents.files.get(category).map {
            it.content.elements.filter { e ->
                e is Name && e.name == name
            }.filter { e ->
                val start = e.location?.range?.startLine
                val end = e.location?.range?.endLine
                val patch = parse(it.patch)
                (start != null && start in patch.get(category)) || (end != null && end in patch.get(category))
            }
        }
            .flatten()
            .filter { it is Name && it.name == name }
    }
}

private fun parse(str: String): Patch {
    var leftLine = 0
    var rightLine = 0
    val patch = Patch()
    try {
        for (line in str.lines()) {
            if (line.startsWith("@@")) {
                val matcher = Pattern.compile("""^@@ -(\d+),?(\d*) \+(\d+),?(\d*) @@.*""").matcher(line)
                if (matcher.matches()) {
                    leftLine = matcher.group(1).toInt()
                    rightLine = matcher.group(3).toInt()
                }
            } else if (line.startsWith("-")) {
                patch.before.add(leftLine++)
            } else if (line.startsWith("+")) {
                patch.after.add(rightLine++)
            } else {
                leftLine++
                rightLine++
            }
        }
        return patch
    } catch (e: Exception) {
        // TODO
    }
    return Patch()
}

private data class Patch(
    val before: MutableList<Int> = mutableListOf(),
    val after: MutableList<Int> = mutableListOf()
) {
    fun get(category: DiffCategory) = if (category == DiffCategory.before) before else after
}
