package com.mmateas.syntax.exception.impl;

import com.mmateas.syntax.exception.SyntacticAnalyzerException;

public class ExpectedExpressionException extends SyntacticAnalyzerException {
    public ExpectedExpressionException(String message) {
        super(message);
    }
}
