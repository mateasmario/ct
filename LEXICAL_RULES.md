```yaml
# identifiers
ID: [a-zA-Z_] [a-zA-Z0-9_]* ;

# keywords
BREAK: 'break' ;
CHAR: 'char' ;
DOUBLE: 'double' ;
ELSE: 'else' ;
FOR: 'for' ;
IF: 'if' ;
INT: 'int' ;
RETURN: 'return' ;
STRUCT: 'struct' ;
VOID: 'void' ;
WHILE: 'while' ;

# constants
CT_INT: [1-9] [0-9]*                       # decimal
        | '0' [0-7]*                               # octal
        | '0x' [0-9a-fA-F]+ ;                  # hexadecimal
fragment EXP: ( 'e' | 'E' ) ( '-' | '+' )? [0-9]+ ;
CT_REAL: [0-9]+ ( '.' [0-9]+ EXP? | ( '.' [0-9]+ )? EXP ) ;
fragment ESC: '\\' [abfnrtv'?"\\0] ;
CT_CHAR: ['] ( ESC | [^'\\] ) ['] ;
CT_STRING: ["] ( ESC | [^"\\] )* ["] ;

# delimiters
COMMA: ',' ;
SEMICOLON: ';' ;
LPAR: '(' ;
RPAR: ')' ;
LBRACKET: '[' ;
RBRACKET: ']' ;
LACC: '{' ;
RACC: '}' ;

# operators
ADD: '+' ;
SUB: '-' ;
MUL: '*' ;
DIV: '/' ;
DOT: '.' ;
AND: '&&' ;
OR: '||' ;
NOT: '!' ;
ASSIGN: '=' ;
EQUAL: '==' ;
NOTEQ: '!=' ;
LESS: '<' ;
LESSEQ: '<=' ;
GREATER: '>' ;
GREATEREQ: '>=' ;

# the following sequences are not important in the next phases and are going
# to be consumed without forming lexical atoms
SPACE: [ \n\r\t] ;
LINECOMMENT: '//' [^\n\r\0]* ;
COMMENT: '/*' ( [^*] | '*'+ [^*/] )* '*'+ '/'  ;

# fragment - just a part of other lexical atoms (for ease of writing)
# does not form a lexical atom itself
```
