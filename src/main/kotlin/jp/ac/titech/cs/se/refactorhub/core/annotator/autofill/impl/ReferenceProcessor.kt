package jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.impl

import jp.ac.titech.cs.se.refactorhub.core.annotator.autofill.AutofillProcessor
import jp.ac.titech.cs.se.refactorhub.core.model.DiffCategory
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.CommitContent
import jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Reference
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElementMetadata
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Name
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration
import java.util.regex.Pattern

class ReferenceProcessor : AutofillProcessor<Reference> {
    override fun process(
        autofill: Reference,
        sourceCategory: DiffCategory,
        sourceElement: CodeElement,
        targetCategory: DiffCategory,
        targetMetadata: CodeElementMetadata,
        content: CommitContent
    ): List<CodeElement> {
        val name = when (sourceElement) {
            is ClassDeclaration -> sourceElement.name
            is FieldDeclaration -> sourceElement.name
            is VariableDeclaration -> sourceElement.name
            else -> return listOf() // TODO
        }
        // TODO
        return content.files.map {
            it.get(targetCategory).content.elements.filter { e ->
                e.type == targetMetadata.type && e is Name && e.name == name
            }.filter { e ->
                val start = e.location?.range?.startLine
                val end = e.location?.range?.endLine
                val patch = parse(it.patch)
                (start != null && start in patch.get(targetCategory)) || (end != null && end in patch.get(targetCategory))
            }
        }
            .flatten()
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
