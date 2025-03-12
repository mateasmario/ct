```yaml
unit: ( declStruct | declFunc | declVar )* END ;


declStruct: STRUCT ID LACC declVar* RACC SEMICOLON ;

declVar:  typeBase ID arrayDecl? ( COMMA ID arrayDecl? )* SEMICOLON ;

typeBase: INT | DOUBLE | CHAR | STRUCT ID ;

arrayDecl: LBRACKET expr? RBRACKET ;

typeName: typeBase arrayDecl? ;

# Din cauza ca in AtomC nu exista pointeri,
# pentru a se respecta sintaxa C care nu permite ca o functie sa returneze vectori
# in AtomC o functie poate returna constructia "tip *", ceea ce e echivalent cu "tip []".
# In acest fel functiile pot returna referinte la vectori

declFunc: ( typeBase MUL? | VOID ) ID 
                        LPAR ( funcArg ( COMMA funcArg )* )? RPAR 
                        stmCompound ;

funcArg: typeBase ID arrayDecl? ;

stm: stmCompound 
           | IF LPAR expr RPAR stm ( ELSE stm )?
           | WHILE LPAR expr RPAR stm
           | FOR LPAR expr? SEMICOLON expr? SEMICOLON expr? RPAR stm
           | BREAK SEMICOLON
           | RETURN expr? SEMICOLON
           | expr? SEMICOLON ;

stmCompound: LACC ( declVar | stm )* RACC ;

expr: exprAssign ;

exprAssign: exprUnary ASSIGN exprAssign | exprOr ;

exprOr: exprOr OR exprAnd | exprAnd ;

exprAnd: exprAnd AND exprEq | exprEq ;

exprEq: exprEq ( EQUAL | NOTEQ ) exprRel | exprRel ;

exprRel: exprRel ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd | exprAdd ;

exprAdd: exprAdd ( ADD | SUB ) exprMul | exprMul ;

exprMul: exprMul ( MUL | DIV ) exprCast | exprCast ;

exprCast: LPAR typeName RPAR exprCast | exprUnary ;

exprUnary: ( SUB | NOT ) exprUnary | exprPostfix ;

exprPostfix: exprPostfix LBRACKET expr RBRACKET
           | exprPostfix DOT ID 
           | exprPrimary ;

exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
           | CT_INT
           | CT_REAL 
           | CT_CHAR 
           | CT_STRING 
           | LPAR expr RPAR ;
```
