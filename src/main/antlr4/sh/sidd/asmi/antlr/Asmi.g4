grammar Asmi;

program : expression+ ;

expression : equality ;

equality : comparison (('!=' | '==') comparison)* ;

comparison : term (('>' | '>=' | '<' | '<=') term)* ;

term : factor (('-' | '+') factor)* ;

factor : unary (('/' | '*') unary)* ;

unary : ('!' | '-') unary | primary ;

primary : INT | FLOAT | STRING | TRUE | FALSE | NULL | '(' expression ')' ;

INT : [0-9]+;
FLOAT : [0-9]+ '.' [0-9]+ ;
STRING : '"' [^"] '"' ;
TRUE : 'true' ;
FALSE : 'false' ;
NULL : 'null' ;
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]+ ;
WHITESPACE : [ \t\r\n]+ -> skip ;