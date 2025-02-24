package com.mmateas.lexer;

import com.mmateas.entity.Token;
import com.mmateas.lexer.exception.LexerException;

import java.util.List;

public interface Lexer {
    List<Token> analyze(String input) throws LexerException;
}
