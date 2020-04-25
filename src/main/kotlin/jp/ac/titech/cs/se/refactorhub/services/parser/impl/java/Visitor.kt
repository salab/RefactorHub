package jp.ac.titech.cs.se.refactorhub.services.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.models.element.data.Location
import jp.ac.titech.cs.se.refactorhub.models.element.data.Range
import jp.ac.titech.cs.se.refactorhub.models.element.impl.*
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.MethodInvocation

class Visitor(
    private val unit: CompilationUnit,
    private val path: String,
    val elements: MutableList<Element> = mutableListOf()
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

    override fun visit(node: FieldDeclaration): Boolean {
        val startPosition = node.startPosition
        node.fragments().forEach {
            val fragment = it as VariableDeclarationFragment
            elements.add(
                jp.ac.titech.cs.se.refactorhub.models.element.impl.FieldDeclaration(
                    fragment.name.identifier,
                    className,
                    Location(path, range(startPosition, fragment.endPosition))
                )
            )
        }
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
            jp.ac.titech.cs.se.refactorhub.models.element.impl.MethodDeclaration(
                node.name.identifier,
                className,
                Location(path, node.range)
            )
        )
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
        }
        if (node.body != null && node.body.statements().isNotEmpty()) {
            val first = node.body.statements().first() as Statement
            val last = node.body.statements().last() as Statement
            elements.add(
                CodeFragments(
                    methodName,
                    className,
                    Location(path, range(first.startPosition, last.endPosition))
                )
            )
        }
        return super.visit(node)
    }

    override fun visit(node: MethodInvocation): Boolean {
        elements.add(
            jp.ac.titech.cs.se.refactorhub.models.element.impl.MethodInvocation(
                methodName,
                className,
                Location(path, node.range)
            )
        )
        return super.visit(node)
    }

    override fun visit(node: VariableDeclarationStatement): Boolean {
        val startPosition = node.startPosition
        node.fragments().forEach {
            val fragment = it as VariableDeclarationFragment
            elements.add(
                jp.ac.titech.cs.se.refactorhub.models.element.impl.VariableDeclaration(
                    fragment.name.identifier,
                    methodName,
                    className,
                    Location(path, range(startPosition, fragment.endPosition))
                )
            )
        }
        return super.visit(node)
    }

    private val ASTNode.endPosition: Int get() = this.startPosition + this.length

    private val ASTNode.range: Range
        get() = range(this.startPosition, this.endPosition)

    private fun range(startPosition: Int, endPosition: Int): Range = Range(
        unit.getLineNumber(startPosition),
        unit.getColumnNumber(startPosition) + 1,
        unit.getLineNumber(endPosition - 1),
        unit.getColumnNumber(endPosition - 1) + 1
    )

}