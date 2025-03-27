package com.mmateas.syntax.impl;

import com.mmateas.entity.Token;
import com.mmateas.syntax.SyntacticAnalyzer;
import com.mmateas.syntax.annotation.HelperMethodForLeftRecursion;
import com.mmateas.syntax.annotation.SyntacticRule;
import com.mmateas.syntax.annotation.SyntacticRuleForLeftRecursion;
import com.mmateas.syntax.exception.SyntacticAnalyzerException;
import com.mmateas.syntax.exception.impl.ExpectedExpressionException;
import com.mmateas.syntax.exception.impl.ExpectedTokenException;
import com.mmateas.syntax.exception.impl.UninitializedTokenListException;

import java.util.List;

public class ResursiveDescendentSyntacticAnalyzer extends SyntacticAnalyzer {
    @Override
    public void analyze() throws SyntacticAnalyzerException {
        if (tokens == null) {
            throw new UninitializedTokenListException("The token list was not explicitly set before starting the analysis.");
        }

        while (currentIndex != tokens.size()) {
            unit();
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
    private boolean unit() throws SyntacticAnalyzerException {
        while (true) {
            if (declStruct()) {

            } else if (declFunc()) {

            } else if (declVar()) {

            } else {
                break;
            }
        }

        if (consume(Token.Type.END)) {
            return true;
        } else {
            throw new ExpectedTokenException("Expected END token at the end of the unit declaration.");
        }
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
                            throw new ExpectedTokenException("Expected ; after struct declaration.");
                        }
                    } else {
                        throw new ExpectedTokenException("Expected } after struct block opening.");
                    }
                } else {
                    throw new ExpectedTokenException("Expected { after struct identifier.");
                }
            } else {
                throw new ExpectedTokenException("Expected identifier after struct keyword.");
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
                            throw new ExpectedTokenException("Expected ID after comma in variable declaration.");
                        }
                    } else {
                        break;
                    }
                }

                if (consume(Token.Type.SEMICOLON)) {
                    return true;
                } else {
                    throw new ExpectedTokenException("Expected semicolon after variable declaration.");
                }
            } else {
                throw new ExpectedTokenException("Expected identifier after type base.");
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
                throw new ExpectedTokenException("Expected identifier after struct keyword.");
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
                throw new ExpectedTokenException("Expression or ] expected after array declaration.");
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

        if (consume(Token.Type.ID)) {
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
                        throw new ExpectedExpressionException("Expected function body.");
                    }
                } else {
                    throw new SyntacticAnalyzerException("Expected ) after function arguments.");
                }
            } else {
                throw new SyntacticAnalyzerException("Expected ( after function identifier.");
            }
        } else {
            currentIndex = startIndex;
            return false;
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
                throw new ExpectedTokenException("Expected identifier after type.");
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
                                throw new ExpectedTokenException("Expected statement after else.");
                            }
                        }

                        return true;
                    } else {
                        throw new ExpectedTokenException("Expected ) after expression or (.");
                    }
                } else {
                    throw new ExpectedExpressionException("Expected expression after (.");
                }
            } else {
                throw new ExpectedTokenException("Expected ( after if keyword.");
            }
        } else if (consume(Token.Type.WHILE)) {
            if (consume(Token.Type.LPAR)) {
                if (expr()) {
                    if (consume(Token.Type.RPAR)) {
                        if (stm()) {
                            return true;
                        } else {
                            throw new ExpectedExpressionException("Expected statement after ).");
                        }
                    } else {
                        throw new ExpectedTokenException("Expected ) after expression.");
                    }
                } else {
                    throw new ExpectedExpressionException("Expected expression after (.");
                }
            } else {
                throw new ExpectedTokenException("Expected ( after while keyword.");
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
                                throw new ExpectedExpressionException("Expected statement after ).");
                            }
                        } else {
                            throw new ExpectedTokenException("Expected ) after for expression.");
                        }
                    } else {
                        throw new ExpectedTokenException("Expected ; after for expression.");
                    }
                } else {
                    throw new ExpectedTokenException("Expected ; after for expression.");
                }
            } else {
                throw new ExpectedTokenException("Expected ( after for keyword.");
            }
        } else if (consume(Token.Type.BREAK)) {
            if (consume(Token.Type.SEMICOLON)) {
                return true;
            } else {
                throw new ExpectedTokenException("Expected ; after break keyword.");
            }
        } else if (consume(Token.Type.RETURN)) {
            expr();
            if (consume(Token.Type.SEMICOLON)) {
                return true;
            } else {
                throw new ExpectedTokenException("Expected ; after return keyword.");
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

    @SyntacticRule("LACC ( declVar | stm )* RACC")
    private boolean stmCompound() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.LACC)) {
            while (true) {
                if (declVar()) {
                } else if (stm()) {

                } else {
                    break;
                }
            }

            if (consume(Token.Type.RACC)) {
                return true;
            } else {
                throw new ExpectedTokenException("Expected } after statement compound.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("exprAssign")
    private boolean expr() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprAssign()) {
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    // This one is implemented a little different, as exprUnary and exprOr have the same prefix
    @SyntacticRule("exprUnary ASSIGN exprAssign | exprOr")
    private boolean exprAssign() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprUnary()) {
            if (consume(Token.Type.ASSIGN)) {
                if (exprAssign()) {
                    return true;
                } else {
                    throw new ExpectedExpressionException("Expected an assignment expression after =.");
                }
            }
        }

        currentIndex = startIndex;

        if (exprOr()) {
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("exprOr OR exprAnd | exprAnd")
    @SyntacticRuleForLeftRecursion("exprAnd exprOr1")
    private boolean exprOr() throws SyntacticAnalyzerException {
        int startToken = currentIndex;

        if (exprAnd()) {
            exprOr1();
            return true;
        } else {
            currentIndex = startToken;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("OR exprAnd exprOr1 | ε")
    private boolean exprOr1() throws SyntacticAnalyzerException {
        int startToken = currentIndex;

        if (consume(Token.Type.OR)) {
            if (exprAnd()) {
                exprOr1();
                return true;
            } else {
                throw new ExpectedExpressionException("Expected AND expression after || operator.");
            }
        } else {
            currentIndex = startToken;
            return false;
        }
    }

    @SyntacticRule("exprAnd AND exprEq | exprEq")
    @SyntacticRuleForLeftRecursion("exprEq exprAnd1")
    private boolean exprAnd() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprEq()) {
            exprAnd1();
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("AND exprEq exprAnd1 | ε")
    private boolean exprAnd1() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.AND)) {
            if (exprEq()) {
                exprAnd1();
                return true;
            } else {
                throw new ExpectedExpressionException("Expected EQUALS expression after &&.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("exprEq (EQUAL | NOTEQ) exprRel | exprRel")
    @SyntacticRuleForLeftRecursion("exprRel exprEq1")
    private boolean exprEq() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprRel()) {
            exprEq1();
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("""
            exprEq ( EQUAL | NOTEQ ) exprRel | exprRel
            | ε
            """)
    private boolean exprEq1() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.EQUAL)) {
            if (exprRel()) {
                exprEq1();
                return true;
            } else {
                throw new ExpectedExpressionException("Expected comparison expression after = operator.");
            }
        } else if (consume(Token.Type.NOTEQ)) {
            if (exprRel()) {
                exprEq1();
                return true;
            } else {
                throw new ExpectedExpressionException("Expected comparison expression after != operator.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("exprRel ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd | exprAdd")
    @SyntacticRuleForLeftRecursion("exprAdd exprRel1")
    private boolean exprRel() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprAdd()) {
            exprRel1();
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("(LESS | LESSEQ | GREATER | GREATEREQ) exprAdd exprRel1 | ε")
    private boolean exprRel1() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.LESS)) {

        } else if (consume(Token.Type.LESSEQ)) {
        } else if (consume(Token.Type.GREATER)) {
        } else if (consume(Token.Type.GREATEREQ)) {
        } else {
            currentIndex = startIndex;
            return false;
        }

        if (exprAdd()) {
            exprRel1();
            return true;
        } else {
            throw new ExpectedExpressionException("Expected addition expression after comparison operator.");
        }
    }

    @SyntacticRule("exprAdd ( ADD | SUB ) exprMul | exprMul")
    @SyntacticRuleForLeftRecursion("exprMul exprAdd1")
    private boolean exprAdd() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprMul()) {
            exprAdd1();
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("""
            (ADD | SUB) exprMul exprAdd1 
            | ε
            """)
    private boolean exprAdd1() throws SyntacticAnalyzerException {
        if (consume(Token.Type.ADD)) {
        } else if (consume(Token.Type.SUB)) {
        } else {
            return false;
        }

        if (exprMul()) {
            exprAdd1();
            return true;
        } else {
            throw new ExpectedExpressionException("Expected multiplication expression after addition or subtraction operator.");
        }
    }

    @SyntacticRule("exprMul (MUL | DIV) exprCast | exprCast")
    @SyntacticRuleForLeftRecursion("exprCast exprMul1")
    private boolean exprMul() throws SyntacticAnalyzerException {
        int startToken = currentIndex;

        if (exprCast()) {
            exprMul1();
            return true;
        } else {
            currentIndex = startToken;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("(MUL | DIV) exprCast exprMul1 | ε")
    private boolean exprMul1() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.MUL)) {
        } else if (consume(Token.Type.DIV)) {
        } else {
            currentIndex = startIndex;
            return false;
        }

        if (exprCast()) {
            exprMul1();
            return true;
        } else {
            throw new ExpectedExpressionException("Expected cast expression after multiplication (or division) operator.");
        }
    }

    @SyntacticRule("LPAR typeName RPAR exprCast | exprUnary")
    private boolean exprCast() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.LPAR)) {
            if (typeName()) {
                if (consume(Token.Type.RPAR)) {
                    if (exprCast()) {
                        return true;
                    } else {
                        throw new ExpectedExpressionException("Expected cast expression after ).");
                    }
                } else {
                    throw new ExpectedTokenException("Expected ) after type name.");
                }
            } else {
                throw new ExpectedExpressionException("Expected type name expression after (.");
            }
        } else {
            currentIndex = startIndex;

            if (exprUnary()) {
                return true;
            } else {
                currentIndex = startIndex;
                return false;
            }
        }
    }

    @SyntacticRule("(SUB | NOT) exprUnary | exprPostfix")
    private boolean exprUnary() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.SUB)) {
            if (exprUnary()) {
                return true;
            } else {
                throw new ExpectedExpressionException("Expected unary expression after - operator.");
            }
        } else if (consume(Token.Type.NOT)) {
            if (exprUnary()) {
                return true;
            } else {
                throw new ExpectedExpressionException("Expected unary expression after ! operator.");
            }
        } else if (exprPostfix()) {
            return true;
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @SyntacticRule("""
            exprPostfix LBRACKET expr RBRACKET
                       | exprPostfix DOT ID
                       | exprPrimary
            """)
    @SyntacticRuleForLeftRecursion("exprPrimary exprPostfix1")
    private boolean exprPostfix() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (exprPrimary()) {
            exprPostfix1();
            return true;

        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    @HelperMethodForLeftRecursion
    @SyntacticRule("""
            LBRACKET expr RBRACKET exprPostfix1
            | DOT ID exprPostfix1
            | ε
            """)
    private boolean exprPostfix1() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.LBRACKET)) {
            if (expr()) {
                if (consume(Token.Type.RBRACKET)) {
                    exprPostfix1();
                    return true;
                } else {
                    throw new ExpectedTokenException("Expected ] after expression.");
                }
            } else {
                throw new ExpectedExpressionException("Expected expression after (.");
            }
        } else if (consume(Token.Type.DOT)) {
            if (consume(Token.Type.ID)) {
                exprPostfix1();
                return true;
            } else {
                throw new ExpectedTokenException("Expected identifier after dot operator.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }

    private boolean exprPrimary() throws SyntacticAnalyzerException {
        int startIndex = currentIndex;

        if (consume(Token.Type.ID)) {
            if (consume(Token.Type.LPAR)) {
                if (expr()) {
                    while (true) {
                        if (consume(Token.Type.COMMA)) {
                            if (expr()) {

                            } else {
                                throw new ExpectedExpressionException("Expected expression after comma (,).");
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (consume(Token.Type.RPAR)) {
                    return true;
                } else {
                    throw new ExpectedTokenException("Expected ) in primary expression.");
                }
            }

            return true;
        } else if (consume(Token.Type.CT_INT)) {
            return true;
        } else if (consume(Token.Type.CT_REAL)) {
            return true;
        } else if (consume(Token.Type.CT_CHAR)) {
            return true;
        } else if (consume(Token.Type.CT_STRING)) {
            return true;
        } else if (consume(Token.Type.LPAR)) {
            if (expr()) {
                if (consume(Token.Type.RPAR)) {
                    return true;
                } else {
                    throw new ExpectedExpressionException("Expected ) after expression.");
                }
            } else {
                throw new ExpectedExpressionException("Expected expression after (.");
            }
        } else {
            currentIndex = startIndex;
            return false;
        }
    }
}
