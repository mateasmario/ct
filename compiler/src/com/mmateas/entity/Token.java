package com.mmateas.entity;

import com.mmateas.lexer.exception.LexerException;
import com.mmateas.lexer.exception.impl.TokenMappingException;

import java.util.List;

public class Token {
    public enum Type {
        ID,
        BREAK,
        CHAR,
        DOUBLE,
        ELSE,
        FOR,
        IF,
        INT,
        RETURN,
        STRUCT,
        VOID,
        WHILE,
        CT_INT,
        CT_REAL,
        CT_CHAR,
        CT_STRING,
        COMMA,
        SEMICOLON,
        LPAR,
        RPAR,
        LBRACKET,
        RBRACKET,
        LACC,
        RACC,
        ADD,
        SUB,
        MULT,
        DIV,
        DOT,
        AND,
        OR,
        NOT,
        ASSIGN,
        EQUAL,
        NOTEQ,
        LESS,
        LESSEQ,
        GREATER,
        GREATEREQ;

        public static final String SYMBOLS = ",;([{}])";

        public static Token.Type fromSymbol(char ch) throws LexerException {
            if (ch == ',') {
                return Token.Type.COMMA;
            } else if (ch == ';') {
                return Token.Type.SEMICOLON;
            } else if (ch == '(') {
                return Token.Type.LPAR;
            } else if (ch == ')') {
                return Token.Type.RPAR;
            } else if (ch == '[') {
                return Token.Type.LBRACKET;
            } else if (ch == ']') {
                return Token.Type.RPAR;
            } else if (ch == '{') {
                return Token.Type.LACC;
            } else if (ch == '}') {
                return Token.Type.RACC;
            }

            throw new TokenMappingException("Cannot map token with value " + ch + " to symbol.");
        }
    }

    private Type type;
    private Object value;

    public Token(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Token(Type type) {
        this.type = type;
    }
}
