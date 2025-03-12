package com.mmateas.syntax.exception.impl;

import com.mmateas.syntax.exception.SyntacticAnalyzerException;

public class UnexpectedTokenException extends SyntacticAnalyzerException {
    public UnexpectedTokenException(String message) {
        super(message);
    }
}
