package jp.ac.titech.cs.se.refactorhub.core.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Location
import jp.ac.titech.cs.se.refactorhub.core.model.element.data.Range
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.CodeFragment
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ConstructorDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldType
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.InterfaceDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ParameterDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ParameterType
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.ReturnType
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableDeclaration
import jp.ac.titech.cs.se.refactorhub.core.model.element.impl.VariableType
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnhancedForStatement
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.PackageDeclaration
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationExpression
import org.eclipse.jdt.core.dom.VariableDeclarationFragment
import org.eclipse.jdt.core.dom.VariableDeclarationStatement

class CodeElementVisitor(
    private val unit: CompilationUnit,
    private val path: String
) : ASTVisitor() {
    val elements: MutableList<CodeElement> = mutableListOf()

    override fun visit(node: PackageDeclaration): Boolean {
        elements.add(
            jp.ac.titech.cs.se.refactorhub.core.model.element.impl.PackageDeclaration(
                node.name.fullyQualifiedName,
                node.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: TypeDeclaration): Boolean {
        val className = getFullyQualifiedClassName(node)
        if (node.isInterface) {
            elements.add(
                InterfaceDeclaration(
                    className,
                    node.location
                )
            )
        } else {
            elements.add(
                ClassDeclaration(
                    className,
                    node.location
                )
            )
        }
        return super.visit(node)
    }

    override fun visit(node: EnumDeclaration): Boolean {
        elements.add(
            jp.ac.titech.cs.se.refactorhub.core.model.element.impl.EnumDeclaration(
                getFullyQualifiedClassName(node),
                node.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: FieldDeclaration): Boolean {
        val className = getFullyQualifiedClassName(node)
        node.fragments().forEach {
            it as VariableDeclarationFragment
            elements.add(
                jp.ac.titech.cs.se.refactorhub.core.model.element.impl.FieldDeclaration(
                    it.name.identifier,
                    className,
                    it.location
                )
            )
        }
        elements.add(
            FieldType(
                node.type.toString(),
                className,
                node.type.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: MethodDeclaration): Boolean {
        val className = getFullyQualifiedClassName(node)
        if (node.isConstructor) {
            elements.add(
                ConstructorDeclaration(
                    node.name.identifier,
                    className,
                    node.location
                )
            )
        }
        elements.add(
            jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodDeclaration(
                node.name.identifier,
                className,
                node.location
            )
        )

        node.returnType2?.let {
            elements.add(
                ReturnType(
                    it.toString(),
                    className,
                    it.location
                )
            )
        }

        val methodName = getMethodName(node)
        node.parameters().forEach {
            val parameter = it as SingleVariableDeclaration
            elements.add(
                ParameterDeclaration(
                    parameter.name.identifier,
                    methodName,
                    className,
                    parameter.location
                )
            )
            elements.add(
                ParameterType(
                    parameter.type.toString(),
                    methodName,
                    className,
                    parameter.type.location
                )
            )
            elements.add(
                VariableDeclaration(
                    parameter.name.identifier,
                    methodName,
                    className,
                    parameter.name.location
                )
            )
        }

        node.body?.let {
            elements.add(
                CodeFragment(
                    methodName,
                    className,
                    Location(path, it.range)
                )
            )
        }

        return super.visit(node)
    }

    override fun visit(node: MethodInvocation): Boolean {
        elements.add(
            jp.ac.titech.cs.se.refactorhub.core.model.element.impl.MethodInvocation(
                getMethodName(node),
                getFullyQualifiedClassName(node),
                node.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: SingleVariableDeclaration): Boolean {
        if (node.parent is EnhancedForStatement) {
            val className = getFullyQualifiedClassName(node)
            val methodName = getMethodName(node)
            elements.add(
                VariableDeclaration(
                    node.name.identifier,
                    methodName,
                    className,
                    node.name.location
                )
            )
            elements.add(
                VariableType(
                    node.type.toString(),
                    methodName,
                    className,
                    node.type.location
                )
            )
        }
        return super.visit(node)
    }

    override fun visit(node: VariableDeclarationStatement): Boolean {
        val className = getFullyQualifiedClassName(node)
        val methodName = getMethodName(node)
        node.fragments().forEach {
            it as VariableDeclarationFragment
            elements.add(
                VariableDeclaration(
                    it.name.identifier,
                    methodName,
                    className,
                    it.location
                )
            )
        }
        elements.add(
            VariableType(
                node.type.toString(),
                methodName,
                className,
                node.type.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: VariableDeclarationExpression): Boolean {
        val className = getFullyQualifiedClassName(node)
        val methodName = getMethodName(node)
        node.fragments().forEach {
            it as VariableDeclarationFragment
            elements.add(
                VariableDeclaration(
                    it.name.identifier,
                    methodName,
                    className,
                    it.location
                )
            )
        }
        elements.add(
            VariableType(
                node.type.toString(),
                methodName,
                className,
                node.type.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: QualifiedName): Boolean {
        elements.add(
            jp.ac.titech.cs.se.refactorhub.core.model.element.impl.QualifiedName(
                node.fullyQualifiedName,
                node.location
            )
        )
        return super.visit(node)
    }

    override fun visit(node: SimpleName): Boolean {
        if (!node.isDeclaration) {
            elements.add(
                jp.ac.titech.cs.se.refactorhub.core.model.element.impl.SimpleName(
                    node.identifier,
                    node.location
                )
            )
        }
        return super.visit(node)
    }

    /* TODO
    override fun visit(node: Block): Boolean {
        node.statements().forEach {
            val statement = it as Statement
            elements.add(
                jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.Statement(
                    methodName,
                    className,
                    Location(path, statement.range)
                )
            )
        }
        return super.visit(node)
    }
    */

    private val ASTNode.endPosition: Int get() = this.startPosition + this.length

    private val ASTNode.range: Range
        get() = getRange(this.startPosition, this.endPosition)

    private val ASTNode.location: Location
        get() = Location(path, this.range)

    private fun getFullyQualifiedClassName(node: ASTNode): String {
        val paths = mutableListOf<String>()
        for (it in generateSequence({ node }, { it.parent })) {
            when (it) {
                is CompilationUnit -> paths.add(it.`package`.name.fullyQualifiedName)
                is AbstractTypeDeclaration -> paths.add(it.name.identifier)
            }
        }
        return paths.reversed().joinToString(".")
    }

    private fun getMethodName(node: ASTNode): String {
        for (it in generateSequence({ node }, { it.parent })) {
            when (it) {
                is MethodDeclaration -> return it.name.identifier
            }
        }
        return ""
    }

    private fun getRange(startPosition: Int, endPosition: Int): Range = Range(
        unit.getLineNumber(startPosition),
        unit.getColumnNumber(startPosition) + 1,
        unit.getLineNumber(endPosition - 1),
        unit.getColumnNumber(endPosition - 1) + 1
    )
}
