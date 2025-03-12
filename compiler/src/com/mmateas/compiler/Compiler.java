package com.mmateas.compiler;

import com.mmateas.compiler.exception.CompilerException;
import com.mmateas.lexer.Lexer;
import com.mmateas.entity.Token;
import com.mmateas.lexer.exception.LexerException;
import com.mmateas.syntax.SyntacticAnalyzer;
import com.mmateas.syntax.exception.SyntacticAnalyzerException;

import java.util.List;

public class Compiler {
    private Lexer lexer;
    private SyntacticAnalyzer syntacticAnalyzer;

    // ...

    public Compiler(Lexer lexer, SyntacticAnalyzer syntacticAnalyzer) {
        this.lexer = lexer;
        this.syntacticAnalyzer = syntacticAnalyzer;
    }

    public void compile(String input) throws CompilerException {
        if (input == null) {
            throw new CompilerException("Provided input is null.");
        }

        List<Token> tokens;

        try {
            tokens = lexer.analyze(input);
        } catch(LexerException ex) {
            throw new CompilerException(ex);
        }

        Token.printList(tokens);

        try {
            syntacticAnalyzer.setTokens(tokens);
            syntacticAnalyzer.analyze();
        } catch (SyntacticAnalyzerException ex) {
            throw new CompilerException(ex);
        }

        // ...
    }
}
