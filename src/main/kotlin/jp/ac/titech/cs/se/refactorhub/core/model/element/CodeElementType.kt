package jp.ac.titech.cs.se.refactorhub.core.model.element

import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Block
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.IfStatement
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SwitchCase
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SwitchStatement
import kotlin.reflect.KClass

enum class CodeElementType {
    PackageDeclaration,

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
    QualifiedName,

    Statement,
    IfStatement,
    SwitchStatement,
    SwitchCase,

    Block,

    CodeFragment;

    val klass: KClass<out CodeElement>
        get() = when (this) {
            PackageDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.PackageDeclaration::class

            //TypeDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.TypeDeclaration::class
            ClassDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ClassDeclaration::class
            InterfaceDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.InterfaceDeclaration::class
            EnumDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.EnumDeclaration::class

            FieldDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration::class
            FieldType -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldType::class

            MethodDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodDeclaration::class
            ConstructorDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ConstructorDeclaration::class
            ReturnType -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ReturnType::class

            ParameterDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ParameterDeclaration::class
            ParameterType -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ParameterType::class

            MethodInvocation -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodInvocation::class

            VariableDeclaration -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration::class
            VariableType -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableType::class

            SimpleName -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SimpleName::class
            QualifiedName -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.QualifiedName::class

            Statement -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Statement::class
            IfStatement -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.IfStatement::class
            SwitchStatement -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SwitchStatement::class
            SwitchCase -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SwitchCase::class

            Block -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.Block::class

            CodeFragment -> jp.ac.titech.cs.se.refactorhub.core.model.element.impl.CodeFragment::class
        }
}
