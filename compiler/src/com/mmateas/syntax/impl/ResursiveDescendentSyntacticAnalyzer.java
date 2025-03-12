package com.mmateas.syntax.impl;

import com.mmateas.entity.Token;
import com.mmateas.syntax.SyntacticAnalyzer;
import com.mmateas.syntax.annotation.SyntacticRule;
import com.mmateas.syntax.exception.SyntacticAnalyzerException;
import com.mmateas.syntax.exception.impl.UnexpectedTokenException;
import com.mmateas.syntax.exception.impl.UninitializedTokenListException;

import java.util.List;

public class ResursiveDescendentSyntacticAnalyzer extends SyntacticAnalyzer {
    @Override
    public void analyze() throws SyntacticAnalyzerException {
        if (tokens == null) {
            throw new UninitializedTokenListException("The token list was not explicitly set before starting the analysis.");
        }

        while(currentIndex != tokens.size() - 1) {
            // ToDo: Other rules...
        }
    }

    @Override
    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    protected boolean consume(Token.Type type) {
        if (tokens.get(currentIndex).getType().equals(type)) {
            consumedIndex = currentIndex++;
            return true;
        }

        return false;
    }

    @SyntacticRule("(declStruct | declFunc | declVar)* END")
    private boolean unit() {
        int startIndex = currentIndex;

        while(true) {
            if (declStruct()) {

            } else if (declFunc()) {

            } else if (declVar()) {

            }
            else {
                break;
            }
        }

        return true;
    }

    @SyntacticRule("STRUCT ID LACC declVar* RACC SEMICOLON")
    private boolean declStruct() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.STRUCT)) {
            if (consume(Token.Type.ID)) {
                if (consume(Token.Type.LACC)) {
                    while(true) {
                        if (declVar()) {
                        }
                        else {
                            break;
                        }
                    }

                    if (consume(Token.Type.RACC)) {
                        if (consume(Token.Type.SEMICOLON)) {
                            return true;
                        }
                        else {
                            throw new UnexpectedTokenException("Expected ; after struct declaration.");
                        }
                    }
                    else {
                        throw new UnexpectedTokenException("Expected } after struct block opening.");
                    }
                }
                else {
                    throw new UnexpectedTokenException("Expected { after struct identifier.");
                }
            }
            else {
                throw new UnexpectedTokenException("Expected identifier after struct keyword.");
            }
        }

        currentIndex = startIndex;
        return false;
    }

    @SyntacticRule("typebase ID arrayDecl? (COMMA ID arrayDecl?)* SEMICOLON")
    private boolean declVar() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (typeBase()) {
            if (consume(Token.Type.ID)) {
                arrayDecl();

                while(true) {
                    if (consume(Token.Type.COMMA)) {
                        if (consume(Token.Type.ID)) {
                            arrayDecl();
                            break;
                        }
                        else {
                            throw new UnexpectedTokenException("Expected ID after comma in variable declaration.");
                        }
                    }
                    else {
                        break;
                    }
                }

                if (consume(Token.Type.SEMICOLON)) {

                }
                else {
                    throw new UnexpectedTokenException("Expected semicolon after variable declaration.");
                }
            }
            else {
                throw new UnexpectedTokenException("Expected identifier after type base.");
            }
        }
        else {
            currentIndex = startIndex;
            return false;
        }
    }
}
