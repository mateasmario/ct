package com.mmateas.lexer.impl;

import com.mmateas.lexer.Lexer;
import com.mmateas.entity.Token;
import com.mmateas.lexer.exception.LexerException;

import java.util.ArrayList;
import java.util.List;

public class StateBasedLexer implements Lexer {
    private enum LexerState {
        ZERO(0),
        ONE(1);

        private int value;

        LexerState(int value) {
            this.value = value;
        }
    }

    private LexerState state = LexerState.ZERO;

    @Override
    public List<Token> analyze(String input) throws LexerException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder tokenValue = new StringBuilder();
        LexerState nextState = LexerState.ZERO;

        int i = 0;

        while (i < input.length()) {
            char ch = input.charAt(i);

            switch (state) {
                case ZERO -> {
                    tokenValue = new StringBuilder();

                    if (Character.isAlphabetic(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.ONE;
                    }
                    else if (Token.Type.SYMBOLS.contains(String.valueOf(ch))) {
                        tokens.add(new Token(Token.Type.fromSymbol(ch)));
                        nextState = LexerState.ZERO;
                    }
                    // ToDo: continue with numbers
                }
                case ONE -> {
                    if (Character.isAlphabetic(ch)
                            || Character.isDigit(ch)
                            || ch == '_') {
                        tokenValue.append(ch);
                        nextState = LexerState.ONE;
                    } else if (Character.isSpaceChar(ch) || Token.Type.SYMBOLS.contains(String.valueOf(ch))) {
                        if (tokenValue.toString().equalsIgnoreCase("break")) {
                            tokens.add(new Token(Token.Type.BREAK));
                        } else if (tokenValue.toString().equalsIgnoreCase("char")) {
                            tokens.add(new Token(Token.Type.CHAR));
                        } else if (tokenValue.toString().equalsIgnoreCase("double")) {
                            tokens.add(new Token(Token.Type.DOUBLE));
                        } else if (tokenValue.toString().equalsIgnoreCase("else")) {
                            tokens.add(new Token(Token.Type.ELSE));
                        } else if (tokenValue.toString().equalsIgnoreCase("for")) {
                            tokens.add(new Token(Token.Type.FOR));
                        } else if (tokenValue.toString().equalsIgnoreCase("if")) {
                            tokens.add(new Token(Token.Type.IF));
                        } else if (tokenValue.toString().equalsIgnoreCase("int")) {
                            tokens.add(new Token(Token.Type.INT));
                        } else if (tokenValue.toString().equalsIgnoreCase("return")) {
                            tokens.add(new Token(Token.Type.RETURN));
                        } else if (tokenValue.toString().equalsIgnoreCase("struct")) {
                            tokens.add(new Token(Token.Type.STRUCT));
                        } else if (tokenValue.toString().equalsIgnoreCase("void")) {
                            tokens.add(new Token(Token.Type.VOID));
                        } else if (tokenValue.toString().equalsIgnoreCase("while")) {
                            tokens.add(new Token(Token.Type.WHILE));
                        } else {
                            tokens.add(new Token(Token.Type.ID, tokenValue));
                        }

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.SYMBOLS.contains(String.valueOf(ch))) {
                            tokens.add(new Token(Token.Type.fromSymbol(ch)));
                        }

                        nextState = LexerState.ZERO;
                    }
                }
            }

            state = nextState;
        }

        return tokens;
    }


}
