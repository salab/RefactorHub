package jp.ac.titech.cs.se.refactorhub.tool.model.element

import kotlin.reflect.KClass

enum class CodeElementType {
    PackageDeclaration,

    TypeDeclaration,
    ClassDeclaration,
    InterfaceDeclaration,
    EnumDeclaration,

    FieldDeclaration,
    FieldType,

    MethodDeclaration,
    ConstructorDeclaration,
    ReturnType,

    ParameterDeclaration,
    ParameterType,

    MethodInvocation,

    VariableDeclaration,
    VariableType,

    SimpleName,

    Statement,

    CodeFragment;

    val klass: KClass<out CodeElement>
        get() = when (this) {
            PackageDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.PackageDeclaration::class

            TypeDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.TypeDeclaration::class
            ClassDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ClassDeclaration::class
            InterfaceDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.InterfaceDeclaration::class
            EnumDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.EnumDeclaration::class

            FieldDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldDeclaration::class
            FieldType -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldType::class

            MethodDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodDeclaration::class
            ConstructorDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ConstructorDeclaration::class
            ReturnType -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ReturnType::class

            ParameterDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ParameterDeclaration::class
            ParameterType -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ParameterType::class

            MethodInvocation -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodInvocation::class

            VariableDeclaration -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableDeclaration::class
            VariableType -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableType::class

            SimpleName -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.SimpleName::class

            Statement -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.Statement::class

            CodeFragment -> jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.CodeFragment::class
        }
}
