package com.mmateas.syntax.exception.impl;

import com.mmateas.syntax.exception.SyntacticAnalyzerException;

public class ExpectedTokenException extends SyntacticAnalyzerException {
    public ExpectedTokenException(String message) {
        super(message);
    }
}
