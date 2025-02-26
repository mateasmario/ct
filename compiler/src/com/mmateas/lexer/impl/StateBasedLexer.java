package com.mmateas.lexer.impl;

import com.mmateas.lexer.Lexer;
import com.mmateas.entity.Token;
import com.mmateas.lexer.exception.LexerException;

import java.util.ArrayList;
import java.util.List;

public class StateBasedLexer implements Lexer {
    private enum LexerState {
        INIT(0),
        LETTER_FIRST(1),
        DIGIT_FIRST(2),
        ZERO_FIRST(3),
        BASE_16_X(4),
        BASE_16_DIGITS(5),
        BASE_8(6),
        REAL_DOT(7),
        REAL_AFTER_DOT(8),
        EXP(9),
        EXP_AFTER_SIGN(10),
        EXP_AFTER_SIGN_MANDATORY_DIGIT(11),
        COMPOSED_OPERATOR(12),
        CHAR(13),
        CHAR_ESC(14),
        CHAR_AFTER_START(15),
        STRING(16),
        STRING_ESC(17);

        private int value;

        LexerState(int value) {
            this.value = value;
        }
    }

    private LexerState state = LexerState.INIT;
    private List<Token> tokens = new ArrayList<>();
    private StringBuilder tokenValue = new StringBuilder();
    private LexerState nextState = LexerState.INIT;
    private int currentLine;

    @Override
    public List<Token> analyze(String input) throws LexerException {
        int i = 0;

        while (i < input.length()) {
            char ch = input.charAt(i);

            if (ch == '\n') {
                currentLine++;
            }

            switch (state) {
                case INIT -> {
                    tokenValue = new StringBuilder();

                    if (Character.isAlphabetic(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.LETTER_FIRST;
                    } else if (Character.isDigit(ch)) {
                        tokenValue.append(ch);

                        if (ch == '0') {
                            nextState = LexerState.ZERO_FIRST;
                        } else {
                            nextState = LexerState.DIGIT_FIRST;
                        }
                    } else if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                        tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        nextState = LexerState.ZERO_FIRST;
                    } else if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                        if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                            tokenValue.append(ch);
                            nextState = LexerState.COMPOSED_OPERATOR;
                        } else {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                            nextState = LexerState.INIT;
                        }
                    }
                    else if (ch == '\'') {
                        nextState = LexerState.CHAR;
                    }
                    else if (ch == '"') {
                        nextState = LexerState.STRING;
                    }
                    else {
                        throw new LexerException("Unexpected character " + ch + " on line " + currentLine + ".");
                    }
                }
                case LETTER_FIRST -> {
                    if (Character.isAlphabetic(ch)
                            || Character.isDigit(ch)
                            || ch == '_') {
                        tokenValue.append(ch);
                        nextState = LexerState.LETTER_FIRST;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Unexpected character " + ch + " on line " + currentLine + ".");
                    }
                }
                case DIGIT_FIRST -> {
                    if (Character.isAlphabetic(ch)) {
                        throw new LexerException("Unexpected character " + ch + " on line " + currentLine + ".");
                    } else if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.DIGIT_FIRST;
                    } else if (ch == '.') {
                        tokenValue.append(ch);
                        nextState = LexerState.REAL_DOT;
                    } else if (ch == 'e' || ch == 'E') {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Wrong number format on line " + currentLine + ".");
                    }
                }
                case ZERO_FIRST -> {
                    if (ch == 'x' || ch == 'X') {
                        tokenValue.append(ch);
                        nextState = LexerState.BASE_16_X;
                    } else if (Character.isDigit(ch)) {
                        if (ch >= '8') {
                            throw new LexerException("Wrong octal number format on line " + currentLine + ".");
                        } else {
                            tokenValue.append(ch);
                            nextState = LexerState.BASE_8;
                        }
                    } else if (ch == '.') {
                        tokenValue.append(ch);
                        nextState = LexerState.REAL_DOT;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Wrong number format on line " + currentLine + ".");
                    }
                }
                case BASE_16_X -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.BASE_16_DIGITS;
                    } else if (Character.isAlphabetic(ch)) {
                        if (ch >= 'A' && ch <= 'F') {
                            tokenValue.append(ch);
                            nextState = LexerState.BASE_16_DIGITS;
                        } else {
                            throw new LexerException("Wrong hexadecimal number format on line " + currentLine + ".");
                        }
                    } else {
                        throw new LexerException("Wrong hexadecimal number format on line " + currentLine + ".");
                    }
                }
                case BASE_16_DIGITS -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.BASE_16_DIGITS;
                    } else if (Character.isAlphabetic(ch)) {
                        if (ch >= 'A' && ch <= 'F') {
                            tokenValue.append(ch);
                            nextState = LexerState.BASE_16_DIGITS;
                        } else {
                            throw new LexerException("Wrong hexadecimal number format on line " + currentLine + ".");
                        }
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Wrong hexadecimal number format on line " + currentLine + ".");
                    }
                }
                case BASE_8 -> {
                    if (Character.isDigit(ch)) {
                        if (ch >= '8') {
                            throw new LexerException("Wrong octal number format on line " + currentLine + ".");
                        } else {
                            tokenValue.append(ch);
                            nextState = LexerState.BASE_8;
                        }
                    } else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        }

                        nextState = LexerState.INIT;
                    } else {
                        throw new LexerException("Wrong octal number format on line " + currentLine + ".");
                    }
                }
                case REAL_DOT -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.REAL_AFTER_DOT;
                    } else {
                        throw new LexerException("Wrong decimal number format on line " + currentLine + ".");
                    }
                }
                case REAL_AFTER_DOT -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.REAL_AFTER_DOT;
                    } else if (ch == 'e' || ch == 'E') {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Wrong decimal number format on line " + currentLine + ".");
                    }
                }
                case EXP -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP_AFTER_SIGN;
                    } else if (ch == '+' || ch == '-') {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP_AFTER_SIGN_MANDATORY_DIGIT;
                    } else {
                        throw new LexerException("Wrong decimal number format on line " + currentLine + ".");
                    }
                }
                case EXP_AFTER_SIGN -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP_AFTER_SIGN;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        manageBothTokens(ch);
                    } else {
                        throw new LexerException("Wrong decimal number format on line " + currentLine + ".");
                    }

                }
                case EXP_AFTER_SIGN_MANDATORY_DIGIT -> {
                    if (Character.isDigit(ch)) {
                        tokenValue.append(ch);
                        nextState = LexerState.EXP_AFTER_SIGN;
                    } else {
                        throw new LexerException("Wrong decimal number format on line " + currentLine + ".");
                    }
                }
                case COMPOSED_OPERATOR -> {
                    if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                        tokens.add(new Token(Token.Type.parse(String.valueOf(tokenValue))));
                    }
                    else {
                        i--;
                    }
                    nextState = LexerState.INIT;
                }
                case CHAR -> {
                    if (ch == '\'') {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    }
                    else if (ch == '\\') {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_ESC;
                    }
                    else  {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_AFTER_START;
                    }
                }
                case CHAR_ESC -> {
                    if (Token.Type.ALLOWED_ESCAPE_CHARACTERS.contains(String.valueOf(ch))) {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_AFTER_START;
                    }
                    else {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    }
                }
                case CHAR_AFTER_START -> {
                    if (ch != '\'') {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    }
                    else {
                        tokens.add(new Token(Token.Type.CT_CHAR, tokenValue.toString().charAt(0)));
                        nextState = LexerState.INIT;
                    }
                }
                case STRING -> {
                    if (ch == '"') {
                        tokens.add(new Token(Token.Type.CT_STRING, tokenValue.toString()));
                        nextState = LexerState.INIT;
                    }
                    else if (ch == '\\') {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING_ESC;
                    }
                    else {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING;
                    }
                }
                case STRING_ESC -> {
                    if (Token.Type.ALLOWED_ESCAPE_CHARACTERS.contains(String.valueOf(ch))) {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING;
                    }
                    else {
                        throw new LexerException("Invalid string format on line " + currentLine + ".");
                    }
                }
            }

            state = nextState;
        }

        return tokens;
    }

    private boolean isNewTokenWithoutSpace(char ch) {
        return Character.isSpaceChar(ch)
                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)
                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR);
    }

    private void manageBothTokens(char ch) {
        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

        // If Symbol came after the built token, also add the symbol to the list of tokens
        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                tokenValue.append(ch);
                nextState = LexerState.COMPOSED_OPERATOR;
            } else {
                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
            }
        }

        nextState = LexerState.INIT;
    }
}
