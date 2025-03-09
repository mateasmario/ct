package com.mmateas;

import com.mmateas.compiler.exception.CompilerException;
import com.mmateas.lexer.Lexer;
import com.mmateas.lexer.impl.StateBasedLexer;
import com.mmateas.compiler.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws CompilerException {
        Lexer lexer = new StateBasedLexer();
        Compiler compiler = new Compiler(lexer);

        List<String> readLines;

        try {
            readLines = Files.readAllLines(Path.of("C:\\git\\ct\\test_programs\\1.c"));
        } catch (IOException e) {
            throw new CompilerException("Could not read specified file.");
        }

        StringBuilder input = new StringBuilder();

        readLines.forEach(
                input::append
        );

        compiler.compile(input.toString());
    }
}
