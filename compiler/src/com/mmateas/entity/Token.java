package com.mmateas.entity;

import java.util.List;

public class Token {
    public enum Type {
        ID(Category.NONE),
        BREAK(Category.KEYWORD, "break"),
        CHAR(Category.KEYWORD, "char"),
        DOUBLE(Category.KEYWORD, "double"),
        ELSE(Category.KEYWORD, "else"),
        FOR(Category.KEYWORD, "for"),
        IF(Category.KEYWORD, "if"),
        INT(Category.KEYWORD, "int"),
        RETURN(Category.KEYWORD, "return"),
        STRUCT(Category.KEYWORD, "struct"),
        VOID(Category.KEYWORD, "void"),
        WHILE(Category.KEYWORD, "while"),
        CT_INT(Category.CONSTANT),
        CT_REAL(Category.CONSTANT),
        CT_CHAR(Category.CONSTANT),
        CT_STRING(Category.CONSTANT),
        COMMA(Category.DELIMITER, ","),
        SEMICOLON(Category.DELIMITER, ";"),
        LPAR(Category.DELIMITER, "("),
        RPAR(Category.DELIMITER, ")"),
        LBRACKET(Category.DELIMITER, "["),
        RBRACKET(Category.DELIMITER, "]"),
        LACC(Category.DELIMITER, "{"),
        RACC(Category.DELIMITER, "}"),
        ADD(Category.OPERATOR, "+"),
        SUB(Category.OPERATOR, "-"),
        MUL(Category.OPERATOR, "*"),
        DIV(Category.OPERATOR, "/"),
        DOT(Category.OPERATOR, "."),
        AND(Category.OPERATOR, "&&"),
        OR(Category.OPERATOR, "||"),
        NOT(Category.OPERATOR, "!"),
        ASSIGN(Category.OPERATOR, "="),
        EQUAL(Category.OPERATOR, "=="),
        NOTEQ(Category.OPERATOR, "!="),
        LESS(Category.OPERATOR, "<"),
        LESSEQ(Category.OPERATOR, "<="),
        GREATER(Category.OPERATOR, ">"),
        GREATEREQ(Category.OPERATOR, ">="),
        SPACE(Category.SPACE, " "),
        NEWLINE(Category.SPACE, "\n"),
        NEWLINE_WINDOWS(Category.SPACE, "\r"),
        TAB(Category.SPACE, "\t");

        public static final String ALLOWED_ESCAPE_CHARACTERS = "abfnrtv'?\"\\0";

        public enum Category {
            NONE, KEYWORD, CONSTANT, DELIMITER, OPERATOR, SPACE
        }

        private Category category;
        private String value;

        Type(Category category) {
            this.category = category;
        }

        Type(Category category, String value) {
            this.category = category;
            this.value = value;
        }

        public static Type parse(String input) {
            for (Type type : Type.values()) {
                if (type.getValue() != null && type.getValue().equalsIgnoreCase(input)) {
                    return type;
                }
            }

            return ID;
        }

        public Category getCategory() {
            return category;
        }

        public String getValue() {
            return value;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static void printList(List<Token> input) {
        for (Token t : input) {
            System.out.print(t.type);

            if ((t.type.getCategory() != null && t.type.getCategory() == Type.Category.CONSTANT)
                    || t.type == Type.ID) {
                System.out.print(" (" + t.value + ")");
            }

            System.out.print("\n");
        }
    }
}
