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

        while (currentIndex != tokens.size() - 1) {
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

        while (true) {
            if (declStruct()) {

            } else if (declFunc()) {

            } else if (declVar()) {

            } else {
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
                    while (true) {
                        if (declVar()) {
                        } else {
                            break;
                        }
                    }

                    if (consume(Token.Type.RACC)) {
                        if (consume(Token.Type.SEMICOLON)) {
                            return true;
                        } else {
                            throw new UnexpectedTokenException("Expected ; after struct declaration.");
                        }
                    } else {
                        throw new UnexpectedTokenException("Expected } after struct block opening.");
                    }
                } else {
                    throw new UnexpectedTokenException("Expected { after struct identifier.");
                }
            } else {
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

                while (true) {
                    if (consume(Token.Type.COMMA)) {
                        if (consume(Token.Type.ID)) {
                            arrayDecl();
                        } else {
                            throw new UnexpectedTokenException("Expected ID after comma in variable declaration.");
                        }
                    } else {
                        break;
                    }
                }

                if (consume(Token.Type.SEMICOLON)) {
                    return true;
                } else {
                    throw new UnexpectedTokenException("Expected semicolon after variable declaration.");
                }
            } else {
                throw new UnexpectedTokenException("Expected identifier after type base.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("INT | DOUBLE | CHAR | STRUCT ID")
    private boolean typeBase() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.INT)) {
            return true;
        } else if (consume(Token.Type.DOUBLE)) {
            return true;
        } else if (consume(Token.Type.CHAR)) {
            return true;
        } else if (consume(Token.Type.STRUCT)) {
            if (consume(Token.Type.ID)) {
                return true;
            } else {
                throw new UnexpectedTokenException("Expected identifier after struct keyword.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("LBRACKET expr? RBRACKET")
    private boolean arrayDecl() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.LBRACKET)) {
            expr();
            if (consume(Token.Type.RBRACKET)) {
                return true;
            } else {
                throw new UnexpectedTokenException("Expression or ] expected after array declaration.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("typeBase arrayDecl?")
    private boolean typeName() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (typeBase()) {
            arrayDecl();
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("""
                ( typeBase MUL? | VOID ) ID
                LPAR ( funcArg ( COMMA funcArg )* )? RPAR
                stmCompound
            """)
    private boolean declFunc() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (typeBase()) {
            consume(Token.Type.MUL);
        } else if (consume(Token.Type.VOID)) {
        } else {
            currentIndex = startIndex;
            return false;
        }

        if (consume(Token.Type.LPAR)) {
            if (funcArg()) {
                while (true) {
                    if (consume(Token.Type.COMMA)) {
                        if (funcArg()) {
                            break;
                        }
                    } else {
                        throw new SyntacticAnalyzerException("Expected function argument after comma.");
                    }
                }
            }

            if (consume(Token.Type.RPAR)) {
                if (stmCompound()) {
                    return true;
                } else {
                    throw new SyntacticAnalyzerException("Expected function body.");
                }
            } else {
                throw new SyntacticAnalyzerException("Expected ) after function arguments.");
            }
        } else {
            throw new SyntacticAnalyzerException("Expected ( after function identifier.");
        }
    }

    @SyntacticRule("typebase ID arrayDecl?")
    private boolean funcArg() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (typeBase()) {
            if (consume(Token.Type.ID)) {
                arrayDecl();
                return true;
            } else {
                throw new UnexpectedTokenException("Expected identifier after type.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("""
                     stmCompound
                        | IF LPAR expr RPAR stm ( ELSE stm )?
                        | WHILE LPAR expr RPAR stm
                        | FOR LPAR expr? SEMICOLON expr? SEMICOLON expr? RPAR stm
                        | BREAK SEMICOLON
                        | RETURN expr? SEMICOLON
                        | expr? SEMICOLON
            """)
    private boolean stm() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (stmCompound()) {
            return true;
        } else if (consume(Token.Type.IF)) {
            if (consume(Token.Type.LPAR)) {
                if (expr()) {
                    if (consume(Token.Type.RPAR)) {
                        if (consume(Token.Type.ELSE)) {
                            if (stm()) {

                            } else {
                                throw new UnexpectedTokenException("Expected statement after else.");
                            }
                        }

                        return true;
                    } else {
                        throw new UnexpectedTokenException("Expected ) after expression or (.");
                    }
                } else {
                    throw new UnexpectedTokenException("Expected expression after (.");
                }
            } else {
                throw new UnexpectedTokenException("Expected ( after if keyword.");
            }
        } else if (consume(Token.Type.WHILE)) {
            if (consume(Token.Type.LPAR)) {
                if (expr()) {
                    if (consume(Token.Type.RPAR)) {
                        if (stm()) {
                            return true;
                        } else {
                            throw new UnexpectedTokenException("Expected statement after ).");
                        }
                    } else {
                        throw new UnexpectedTokenException("Expected ) after expression.");
                    }
                } else {
                    throw new UnexpectedTokenException("Expected expression after (.");
                }
            } else {
                throw new UnexpectedTokenException("Expected ( after while keyword.");
            }
        } else if (consume(Token.Type.FOR)) {
            if (consume(Token.Type.LPAR)) {
                expr();
                if (consume(Token.Type.SEMICOLON)) {
                    expr();
                    if (consume(Token.Type.SEMICOLON)) {
                        expr();
                        if (consume(Token.Type.RPAR)) {
                            if (stm()) {
                                return true;
                            } else {
                                throw new UnexpectedTokenException("Expected statement after ).");
                            }
                        } else {
                            throw new UnexpectedTokenException("Expected ) after for expression.");
                        }
                    } else {
                        throw new UnexpectedTokenException("Expected ; after for expression.");
                    }
                } else {
                    throw new UnexpectedTokenException("Expected ; after for expression.");
                }
            } else {
                throw new UnexpectedTokenException("Expected ( after for keyword.");
            }
        } else if (consume(Token.Type.BREAK)) {
            if (consume(Token.Type.SEMICOLON)) {
                return true;
            } else {
                throw new UnexpectedTokenException("Expected ; after break keyword.");
            }
        } else if (consume(Token.Type.RETURN)) {
            expr();
            if (consume(Token.Type.SEMICOLON)) {
                return true;
            } else {
                throw new UnexpectedTokenException("Expected ; after return keyword.");
            }
        } else {
            expr();
            if (consume(Token.Type.SEMICOLON)) {
                return true;
            } else {
                currentIndex = startIndex;
                return false;
            }
        }
    }

    // ToDo: Continue with stmCompound, etc...
}
