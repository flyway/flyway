grammar BooleanExpression;

program
 : expression EOF
 ;

expression
 : LEFT_PAREN expression RIGHT_PAREN                   # parenExpression
 | left=expression op=comparator right=expression      # comparatorExpression
 | left=expression op=logicalOperator right=expression # logicalOperatorExpression
 | left=WORD op=comparator right=WORD                  # wordcomparatorExpression
 | left=WORD op=comparator right=expression            # leftWordcomparatorExpression
 | left=expression op=comparator right=WORD            # rightWordcomparatorExpression
 | bool                                                # boolExpression
 ;

comparator
 : EQUAL | NOT_EQUAL
 ;

logicalOperator
 : AND | OR
 ;

bool
 : TRUE | FALSE
 ;

AND                                     : '&&' ;
OR                                      : '||' ;
TRUE options { caseInsensitive=true; }  : 'TRUE' ;
FALSE options { caseInsensitive=true; } : 'FALSE' ;
EQUAL                                   : '==' ;
NOT_EQUAL                               : '!=' ;
LEFT_PAREN                              : '(' ;
RIGHT_PAREN                             : ')' ;
WORD                                    : ~[\p{White_Space}&|=!()]+ ;
WHITESPACE                              : [\p{White_Space}]+ -> skip ;
