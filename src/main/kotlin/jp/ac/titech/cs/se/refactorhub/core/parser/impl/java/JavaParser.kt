package jp.ac.titech.cs.se.refactorhub.core.parser.impl.java

import jp.ac.titech.cs.se.refactorhub.core.model.annotator.Token
import jp.ac.titech.cs.se.refactorhub.core.model.element.CodeElement
import jp.ac.titech.cs.se.refactorhub.core.parser.CodeParser
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.compiler.ITerminalSymbols
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit

class JavaParser : CodeParser {

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

    override fun tokenize(text: String): List<Token> {
        val scanner = ToolFactory.createScanner(true, false, false, JavaCore.VERSION_15)
        scanner.source = text.toCharArray()
        val tokens = ArrayList<Token>()
        var nextTokenType = scanner.nextToken
        while (nextTokenType != ITerminalSymbols.TokenNameEOF) {
            val raw = String(scanner.currentTokenSource)
            val start = scanner.currentTokenStartPosition
            val end = scanner.currentTokenEndPosition
            tokens.add(Token(raw, start, end, isSymbol(nextTokenType), isComment(nextTokenType)))
            nextTokenType = scanner.nextToken
        }
        return tokens
    }
}

private fun isComment(tokenType: Int): Boolean {
    return comments.contains(tokenType)
}
private val comments = listOf(
    ITerminalSymbols.TokenNameCOMMENT_LINE,
    ITerminalSymbols.TokenNameCOMMENT_BLOCK,
    ITerminalSymbols.TokenNameCOMMENT_JAVADOC
)

private fun isSymbol(tokenType: Int): Boolean {
    return symbols.contains(tokenType)
}
private val symbols = listOf(
    ITerminalSymbols.TokenNameWHITESPACE,
    ITerminalSymbols.TokenNamePLUS_PLUS,
    ITerminalSymbols.TokenNameMINUS_MINUS,
    ITerminalSymbols.TokenNameEQUAL_EQUAL,
    ITerminalSymbols.TokenNameLESS_EQUAL ,
    ITerminalSymbols.TokenNameGREATER_EQUAL ,
    ITerminalSymbols.TokenNameNOT_EQUAL ,
    ITerminalSymbols.TokenNameLEFT_SHIFT ,
    ITerminalSymbols.TokenNameRIGHT_SHIFT ,
    ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT ,
    ITerminalSymbols.TokenNamePLUS_EQUAL,
    ITerminalSymbols.TokenNameMINUS_EQUAL,
    ITerminalSymbols.TokenNameMULTIPLY_EQUAL,
    ITerminalSymbols.TokenNameDIVIDE_EQUAL,
    ITerminalSymbols.TokenNameAND_EQUAL,
    ITerminalSymbols.TokenNameOR_EQUAL,
    ITerminalSymbols.TokenNameXOR_EQUAL,
    ITerminalSymbols.TokenNameREMAINDER_EQUAL,
    ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL,
    ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL,
    ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL,
    ITerminalSymbols.TokenNameOR_OR,
    ITerminalSymbols.TokenNameAND_AND,
    ITerminalSymbols.TokenNamePLUS,
    ITerminalSymbols.TokenNameMINUS,
    ITerminalSymbols.TokenNameNOT,
    ITerminalSymbols.TokenNameREMAINDER,
    ITerminalSymbols.TokenNameXOR,
    ITerminalSymbols.TokenNameAND,
    ITerminalSymbols.TokenNameMULTIPLY,
    ITerminalSymbols.TokenNameOR,
    ITerminalSymbols.TokenNameTWIDDLE,
    ITerminalSymbols.TokenNameDIVIDE,
    ITerminalSymbols.TokenNameGREATER,
    ITerminalSymbols.TokenNameLESS,
    ITerminalSymbols.TokenNameLPAREN,
    ITerminalSymbols.TokenNameRPAREN,
    ITerminalSymbols.TokenNameLBRACE,
    ITerminalSymbols.TokenNameRBRACE,
    ITerminalSymbols.TokenNameLBRACKET,
    ITerminalSymbols.TokenNameRBRACKET,
    ITerminalSymbols.TokenNameSEMICOLON,
    ITerminalSymbols.TokenNameQUESTION,
    ITerminalSymbols.TokenNameCOLON,
    ITerminalSymbols.TokenNameCOMMA,
    ITerminalSymbols.TokenNameDOT,
    ITerminalSymbols.TokenNameEQUAL,
    ITerminalSymbols.TokenNameEOF,
    ITerminalSymbols.TokenNameAT,
    ITerminalSymbols.TokenNameELLIPSIS,
    ITerminalSymbols.TokenNameARROW,
    ITerminalSymbols.TokenNameCOLON_COLON
)

