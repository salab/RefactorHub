package jp.ac.titech.cs.se.refactorhub.tool.model.element

import kotlin.reflect.KClass

enum class CodeElementType {
    ClassDeclaration,
    ConstructorDeclaration,
    FieldDeclaration,
    InterfaceDeclaration,
    MethodDeclaration,
    MethodInvocation,
    ParameterDeclaration,
    VariableDeclaration,
    CodeFragment;

    val klass: KClass<out CodeElement>
        get() = when (this) {
            ClassDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ClassDeclaration::class
            ConstructorDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ConstructorDeclaration::class
            FieldDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldDeclaration::class
            InterfaceDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.InterfaceDeclaration::class
            MethodDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodDeclaration::class
            MethodInvocation -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodInvocation::class
            ParameterDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ParameterDeclaration::class
            VariableDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableDeclaration::class
            CodeFragment -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.CodeFragment::class
        }
}
