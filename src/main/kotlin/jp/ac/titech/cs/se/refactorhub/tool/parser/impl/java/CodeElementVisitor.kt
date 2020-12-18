package jp.ac.titech.cs.se.refactorhub.tool.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.tool.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Location
import jp.ac.titech.cs.se.refactorhub.tool.model.element.data.Range
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ClassDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.CodeFragment
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ConstructorDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.InterfaceDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ParameterDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ParameterType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.ReturnType
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableDeclaration
import jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.VariableType
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.PackageDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationExpression
import org.eclipse.jdt.core.dom.VariableDeclarationFragment

class CodeElementVisitor(
    private val unit: CompilationUnit,
    private val path: String,
    val elements: MutableList<CodeElement> = mutableListOf()
) : ASTVisitor() {
    private var packageName = ""
    private var className = ""
    private var methodName = ""

    override fun visit(node: PackageDeclaration): Boolean {
        packageName = node.name.fullyQualifiedName
        return super.visit(node)
    }

    override fun visit(node: TypeDeclaration): Boolean {
        className = "$packageName.${node.name.identifier}"
        if (node.isInterface) {
            elements.add(
                InterfaceDeclaration(
                    className,
                    Location(path, node.range)
                )
            )
        } else {
            elements.add(
                ClassDeclaration(
                    className,
                    Location(path, node.range)
                )
            )
        }
        return super.visit(node)
    }

    override fun visit(node: EnumDeclaration): Boolean {
        className = "$packageName.${node.name.identifier}"
        elements.add(
            jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.EnumDeclaration(
                className,
                Location(path, node.range)
            )
        )
        return super.visit(node)
    }

    override fun visit(node: FieldDeclaration): Boolean {
        node.fragments().forEach {
            it as VariableDeclarationFragment
            elements.add(
                jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.FieldDeclaration(
                    it.name.identifier,
                    className,
                    Location(path, it.range)
                )
            )
        }
        elements.add(
            FieldType(
                node.type.toString(),
                className,
                Location(path, node.type.range)
            )
        )
        return super.visit(node)
    }

    override fun visit(node: MethodDeclaration): Boolean {
        if (node.isConstructor) {
            elements.add(
                ConstructorDeclaration(
                    node.name.identifier,
                    className,
                    Location(path, node.range)
                )
            )
        }
        elements.add(
            jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodDeclaration(
                node.name.identifier,
                className,
                Location(path, node.range)
            )
        )

        node.returnType2?.let {
            elements.add(
                ReturnType(
                    it.toString(),
                    className,
                    Location(path, it.range)
                )
            )
        }

        methodName = node.name.identifier
        node.parameters().forEach {
            val parameter = it as SingleVariableDeclaration
            elements.add(
                ParameterDeclaration(
                    parameter.name.identifier,
                    methodName,
                    className,
                    Location(path, parameter.range)
                )
            )
            elements.add(
                ParameterType(
                    parameter.type.toString(),
                    methodName,
                    className,
                    Location(path, parameter.type.range)
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
            jp.ac.titech.cs.se.refactorhub.tool.model.element.impl.MethodInvocation(
                methodName,
                className,
                Location(path, node.range)
            )
        )
        return super.visit(node)
    }

    override fun visit(node: VariableDeclarationExpression): Boolean {
        node.fragments().forEach {
            it as VariableDeclarationFragment
            elements.add(
                VariableDeclaration(
                    it.name.identifier,
                    methodName,
                    className,
                    Location(path, it.range)
                )
            )
        }
        elements.add(
            VariableType(
                node.type.toString(),
                methodName,
                className,
                Location(
                    path, node.type.range
                )
            )
        )
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

    private fun getRange(startPosition: Int, endPosition: Int): Range = Range(
        unit.getLineNumber(startPosition),
        unit.getColumnNumber(startPosition) + 1,
        unit.getLineNumber(endPosition - 1),
        unit.getColumnNumber(endPosition - 1) + 1
    )
}
