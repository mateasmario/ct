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
        BASE_8(6);

        private int value;

        LexerState(int value) {
            this.value = value;
        }
    }

    private LexerState state = LexerState.INIT;

    @Override
    public List<Token> analyze(String input) throws LexerException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder tokenValue = new StringBuilder();
        LexerState nextState = LexerState.INIT;
        int currentLine = 0;

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
                    } else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.ID, tokenValue.toString()));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
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
                    } else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        }

                        nextState = LexerState.INIT;
                    }
                    // ToDo: Manage DOT (for REAL)
                    else {
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
                    }
                    // ToDo: Manage DOT (for REAL)
                    else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        }

                        nextState = LexerState.INIT;
                    }
                    else {
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
                    else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        }

                        nextState = LexerState.INIT;
                    }
                    else {
                        throw new LexerException("Wrong hexadecimal number format on line " + currentLine + ".");
                    }
                }
                case BASE_8 -> {
                    if (Character.isDigit(ch)) {
                        if (ch >= '8') {
                            throw new LexerException("Wrong octal number format on line " + currentLine + ".");
                        }
                        else {
                            tokenValue.append(ch);
                            nextState = LexerState.BASE_8;
                        }
                    }
                    else if (Character.isSpaceChar(ch)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)
                            || Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.SPACE)) {
                        tokens.add(new Token(Token.Type.INT, Integer.valueOf(tokenValue.toString())));

                        // If Symbol came after the built token, also add the symbol to the list of tokens
                        if (Token.Type.parse(String.valueOf(ch)).getCategory().equals(Token.Type.Category.DELIMITER)) {
                            tokens.add(new Token(Token.Type.parse(String.valueOf(ch))));
                        }

                        nextState = LexerState.INIT;
                    }
                    else {
                        throw new LexerException("Wrong octal number format on line " + currentLine + ".");
                    }
                }
            }

            state = nextState;
        }

        return tokens;
    }


}
