package jp.ac.titech.cs.se.refactorhub.old.services.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.old.models.element.Element
import jp.ac.titech.cs.se.refactorhub.old.services.parser.Parser
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit

class JavaParser : Parser {

    override fun parse(text: String, path: String): List<Element> {
        val parser = ASTParser.newParser(AST.JLS12)
        parser.setSource(text.toCharArray())
        val unit = parser.createAST(NullProgressMonitor()) as CompilationUnit
        val visitor = Visitor(unit, path)
        unit.accept(visitor)
        return visitor.elements
    }
}
