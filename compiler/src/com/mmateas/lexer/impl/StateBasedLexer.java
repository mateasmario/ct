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
        STRING_ESC(17),
        SLASH(18),
        LINE_COMMENT(19),
        MULTILINE_COMMENT(20),
        MULTILINE_COMMENT_CLOSING_STAR(21),
        AND_START(22),
        OR_START(23);

        private int value;

        LexerState(int value) {
            this.value = value;
        }
    }

    @Override
    public List<Token> analyze(String input) throws LexerException {
        int i = 0;
        LexerState state = LexerState.INIT;
        List<Token> tokens = new ArrayList<>();
        StringBuilder tokenValue = new StringBuilder();
        LexerState nextState = LexerState.INIT;
        int currentLine = 0;

        while (i <= input.length()) {
            char ch = i == input.length() ? 0 : input.charAt(i);

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
                        nextState = LexerState.INIT;
                    } else if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                        if (ch == '/') {
                            nextState = LexerState.SLASH;
                        }
                        else if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                            tokenValue.append(ch);
                            nextState = LexerState.COMPOSED_OPERATOR;
                        } else {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                            nextState = LexerState.INIT;
                        }
                    } else if (ch == '\'') {
                        nextState = LexerState.CHAR;
                    } else if (ch == '"') {
                        nextState = LexerState.STRING;
                    } else if (ch == '&') {
                        nextState = LexerState.AND_START;
                    } else if (ch == '|') {
                        nextState = LexerState.OR_START;
                    } else if (isSpaceChar(ch)) {
                        nextState = LexerState.INIT;
                    } else {
                        throw new LexerException("Unexpected character " + ch + " on line " + currentLine + ".");
                    }
                }
                case LETTER_FIRST -> {
                    if (Character.isAlphabetic(ch)
                            || Character.isDigit(ch)
                            || ch == '_') {
                        tokenValue.append(ch);
                        nextState = LexerState.LETTER_FIRST;
                    } else if (ch == '&') {
                        Token.Type tokenType = Token.Type.parse(tokenValue.toString());

                        if (tokenType == Token.Type.ID) {
                            tokens.add(new Token(tokenType, tokenValue.toString()));
                        } else {
                            tokens.add(new Token(tokenType));
                        }

                        nextState = LexerState.AND_START;
                    } else if (ch == '|') {
                        Token.Type tokenType = Token.Type.parse(tokenValue.toString());

                        if (tokenType == Token.Type.ID) {
                            tokens.add(new Token(tokenType, tokenValue.toString()));
                        } else {
                            tokens.add(new Token(tokenType));
                        }

                        nextState = LexerState.OR_START;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        Token.Type tokenType = Token.Type.parse(tokenValue.toString());

                        if (tokenType == Token.Type.ID) {
                            tokens.add(new Token(tokenType, tokenValue.toString()));
                        } else {
                            tokens.add(new Token(tokenType));
                        }

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue = new StringBuilder();
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        Token.Type tokenType = Token.Type.parse(tokenValue.toString());

                        if (tokenType == Token.Type.ID) {
                            tokens.add(new Token(tokenType, tokenValue.toString()));
                        } else {
                            tokens.add(new Token(tokenType));
                        }

                        nextState = LexerState.INIT;
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
                    } else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    } else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.INIT;
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
                    } else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.AND_START;
                    } else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    } else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.INIT;
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
                    }
                    else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.AND_START;
                    }
                    else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    }
                    else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.INIT;
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
                    }
                    else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.AND_START;
                    }
                    else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    }
                    else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_INT, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
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
                    }
                    else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.AND_START;
                    }
                    else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    }
                    else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.INIT;
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
                    }
                    else if (ch == '&') {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.AND_START;
                    }
                    else if (ch == '|') {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.OR_START;
                    }
                    else if (isNewTokenWithoutSpace(ch)) {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                                || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.OPERATOR)) {
                            if (ch == '=' || ch == '!' || ch == '<' || ch == '>') {
                                tokenValue.append(ch);
                                nextState = LexerState.COMPOSED_OPERATOR;
                            } else {
                                tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                                nextState = LexerState.INIT;
                            }
                        }
                    } else if (isSpaceChar(ch)) {
                        tokens.add(new Token(Token.Type.CT_REAL, tokenValue.toString()));
                        nextState = LexerState.INIT;
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
                    if (Token.Type.parse(tokenValue.toString() + ch).getCategory().equals(Token.Type.Category.OPERATOR)) {
                        tokenValue.append(ch);
                    } else {
                        i--;
                    }

                    tokens.add(new Token(Token.Type.parse(String.valueOf(tokenValue))));
                    nextState = LexerState.INIT;
                }
                case CHAR -> {
                    if (ch == '\'') {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    } else if (ch == '\\') {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_ESC;
                    } else {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_AFTER_START;
                    }
                }
                case CHAR_ESC -> {
                    if (Token.Type.ALLOWED_ESCAPE_CHARACTERS.contains(String.valueOf(ch))) {
                        tokenValue.append(ch);
                        nextState = LexerState.CHAR_AFTER_START;
                    } else {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    }
                }
                case CHAR_AFTER_START -> {
                    if (ch != '\'') {
                        throw new LexerException("Invalid character format on line " + currentLine + ".");
                    } else {
                        tokens.add(new Token(Token.Type.CT_CHAR, tokenValue.toString().charAt(0)));
                        nextState = LexerState.INIT;
                    }
                }
                case STRING -> {
                    if (ch == '"') {
                        tokens.add(new Token(Token.Type.CT_STRING, tokenValue.toString()));
                        nextState = LexerState.INIT;
                    } else if (ch == '\\') {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING_ESC;
                    } else {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING;
                    }
                }
                case STRING_ESC -> {
                    if (Token.Type.ALLOWED_ESCAPE_CHARACTERS.contains(String.valueOf(ch))) {
                        tokenValue.append(ch);
                        nextState = LexerState.STRING;
                    } else {
                        throw new LexerException("Invalid string format on line " + currentLine + ".");
                    }
                }
                case SLASH -> {
                    if (ch == '/') {
                        nextState = LexerState.LINE_COMMENT;
                    } else if (ch == '*') {
                        nextState = LexerState.MULTILINE_COMMENT;
                    } else {
                        tokens.add(new Token(Token.Type.DIV, null));

                        i--;
                        nextState = LexerState.INIT;
                    }
                }
                case LINE_COMMENT -> {
                    if (ch == '\n' || ch == 0) {
                        nextState = LexerState.INIT;
                    } else {
                        nextState = LexerState.LINE_COMMENT;
                    }
                }
                case MULTILINE_COMMENT -> {
                    if (ch == '*') {
                        nextState = LexerState.MULTILINE_COMMENT_CLOSING_STAR;
                    } else {
                        nextState = LexerState.MULTILINE_COMMENT;
                    }
                }
                case MULTILINE_COMMENT_CLOSING_STAR -> {
                    if (ch == '/') {
                        nextState = LexerState.INIT;
                    } else if (ch == '*') {
                        nextState = LexerState.MULTILINE_COMMENT_CLOSING_STAR;
                    } else {
                        nextState = LexerState.MULTILINE_COMMENT;
                    }
                }
                case AND_START -> {
                    if (ch == '&') {
                        tokens.add(new Token(Token.Type.AND));
                        nextState = LexerState.INIT;
                    }
                    else {
                        throw new LexerException("Expected '&&', not '&' on line " + currentLine + ". Bitwise operations are not supported by AtomC.");
                    }
                }
                case OR_START -> {
                    if (ch == '|') {
                        tokens.add(new Token(Token.Type.OR));
                        nextState = LexerState.INIT;
                    }
                    else {
                        throw new LexerException("Expected '||', not '|' on line " + currentLine + ". Bitwise operations are not supported by AtomC.");
                    }
                }
            }

            state = nextState;
            i++;
        }

        return tokens;
    }

    private boolean isNewTokenWithoutSpace(char ch) {
        Token.Type tokenType = Token.Type.parse(String.valueOf(ch));

        if (tokenType.getCategory() == null) {
            return false;
        }

        return tokenType.getCategory().equals(Token.Type.Category.DELIMITER)
                || tokenType.getCategory().equals(Token.Type.Category.OPERATOR);
    }

    private boolean isSpaceChar(char ch) {
        return ch == 0 || Character.isWhitespace(ch);
    }
}
