package jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill

import kotlin.reflect.KClass

enum class AutofillType {
    Reference,
    Surround,
    Package;

    val klass: KClass<out Autofill>
        get() = when (this) {
            Reference -> jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Reference::class
            Surround -> jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Surround::class
            Package -> jp.ac.titech.cs.se.refactorhub.core.model.annotator.autofill.impl.Package::class
        }
}
