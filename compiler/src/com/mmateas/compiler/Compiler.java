package com.mmateas.compiler;

import com.mmateas.compiler.exception.CompilerException;
import com.mmateas.lexer.Lexer;
import com.mmateas.entity.Token;
import com.mmateas.lexer.exception.LexerException;

import java.util.List;

public class Compiler {
    private Lexer lexer;

    // ...

    public Compiler(Lexer lexer) {
        this.lexer = lexer;
    }

    public void compile(String input) throws CompilerException {
        if (input == null) {
            throw new CompilerException("Provided input is null.");
        }

        List<Token> tokens;

        try {
            tokens = lexer.analyze(input);
        } catch(LexerException ex) {
            throw new CompilerException(ex);
        }

        Token.printList(tokens);

        // ...
    }
}
