package com.mmateas.syntax;

import com.mmateas.entity.Token;
import com.mmateas.syntax.exception.SyntacticAnalyzerException;

import java.util.List;

public abstract class SyntacticAnalyzer {
    protected List<Token> tokens;
    protected int currentIndex;
    protected int consumedIndex;
    public abstract void analyze() throws SyntacticAnalyzerException;
    public abstract void setTokens(List<Token> tokens);
    protected abstract boolean consume(Token.Type type);
}
