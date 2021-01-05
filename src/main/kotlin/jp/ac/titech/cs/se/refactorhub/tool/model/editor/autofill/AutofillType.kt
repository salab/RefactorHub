package jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill

import kotlin.reflect.KClass

enum class AutofillType {
    Reference,
    Surround,
    Package;

    val klass: KClass<out Autofill>
        get() = when (this) {
            Reference -> jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Reference::class
            Surround -> jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Surround::class
            Package -> jp.ac.titech.cs.se.refactorhub.tool.model.editor.autofill.impl.Package::class
        }
}
