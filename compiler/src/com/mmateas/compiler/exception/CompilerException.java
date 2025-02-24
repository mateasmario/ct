package com.mmateas.compiler.exception;

import com.mmateas.lexer.exception.LexerException;

public class CompilerException extends Exception {
    public CompilerException(String message) {
        super(message);
    }

    public CompilerException(LexerException ex) {
        super(ex.getMessage());
    }
}
