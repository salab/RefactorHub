package jp.ac.titech.cs.se.refactorhub.core.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.parser.CodeElementParser
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit

class JavaParser : CodeElementParser {

    override fun parse(text: String, path: String): List<CodeElement> {
        val parser = ASTParser.newParser(AST.JLS_Latest).apply {
            val options = JavaCore.getOptions().apply {
                this[JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM] = JavaCore.VERSION_14
                this[JavaCore.COMPILER_SOURCE] = JavaCore.VERSION_14
                this[JavaCore.COMPILER_COMPLIANCE] = JavaCore.VERSION_14
            }
            this.setCompilerOptions(options)
            this.setResolveBindings(false)
            this.setKind(ASTParser.K_COMPILATION_UNIT)
            this.setStatementsRecovery(true)
            this.setSource(text.toCharArray())
        }
        val unit = parser.createAST(NullProgressMonitor()) as CompilationUnit
        val visitor = CodeElementVisitor(unit, path)
        unit.accept(visitor)
        return visitor.elements
    }
}
