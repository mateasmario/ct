package com.mmateas;

import com.mmateas.compiler.exception.CompilerException;
import com.mmateas.lexer.Lexer;
import com.mmateas.lexer.impl.StateBasedLexer;
import com.mmateas.compiler.Compiler;

public class Main {

    public static void main(String[] args) throws CompilerException {
        Lexer lexer = new StateBasedLexer();
        Compiler compiler = new Compiler(lexer);

        // ToDo: Change with file input
        compiler.compile(null);
    }
}
