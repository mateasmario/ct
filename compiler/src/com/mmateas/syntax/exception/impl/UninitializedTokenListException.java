package com.mmateas.syntax.exception.impl;

import com.mmateas.syntax.exception.SyntacticAnalyzerException;

public class UninitializedTokenListException extends SyntacticAnalyzerException {
    public UninitializedTokenListException(String message) {
        super(message);
    }
}
