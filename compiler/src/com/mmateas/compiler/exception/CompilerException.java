package com.mmateas.compiler.exception;

import com.mmateas.lexer.exception.LexerException;
import com.mmateas.syntax.exception.SyntacticAnalyzerException;

public class CompilerException extends Exception {
    public CompilerException(String message) {
        super(message);
    }

    public CompilerException(LexerException ex) {
        super(ex.getMessage());
    }

    public CompilerException(SyntacticAnalyzerException ex) {
        super(ex.getMessage());
    }
}
