// @author Roman Manevich
package jminor.ast;

/** A scanner for Jminor synthesis problems.
 * @author Roman Manevich
 */
%%

%class JminorLexer
%cupsym JminorSym
%cup
%public
%type Token
%line
%column
%scanerror LexicalError

%{
StringBuffer string = new StringBuffer();

public int getLineNumber() { return yyline + 1; }
%}

%eofval{
  return new Token(JminorSym.EOF, yytext(), yyline, yycolumn);
%eofval}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
Letter 		= [a-zA-Z]
Digit 		= [0-9]
PosDigit	= [1-9]
Id 			= ({Letter}|_)({Letter}|_|{Digit})*
Int			= 0|{PosDigit}{Digit}*|-{PosDigit}{Digit}*

EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
TraditionalComment   = "/*" ~"*/" | "/*" "*"+ "/"

%%

<YYINITIAL> {
  "type"		{ return new Token(JminorSym.TYPE, yytext(), yyline, yycolumn); }
  "example"		{ return new Token(JminorSym.EXAMPLE, yytext(), yyline, yycolumn); }
  "test"		{ return new Token(JminorSym.TEST, yytext(), yyline, yycolumn); }
  "var"			{ return new Token(JminorSym.VAR, yytext(), yyline, yycolumn); }
  "null"		{ return new Token(JminorSym.NULL, yytext(), yyline, yycolumn); }
  "mut"			{ return new Token(JminorSym.MUT, yytext(), yyline, yycolumn); }
  "ghost"		{ return new Token(JminorSym.GHOST, yytext(), yyline, yycolumn); }

  "if"			{ return new Token(JminorSym.IF, yytext(), yyline, yycolumn); }
  "else"		{ return new Token(JminorSym.ELSE, yytext(), yyline, yycolumn); }  
  "while"		{ return new Token(JminorSym.WHILE, yytext(), yyline, yycolumn); }
  "true"		{ return new Token(JminorSym.BOOLEAN_VAL, true, yyline, yycolumn); }
  "false"		{ return new Token(JminorSym.BOOLEAN_VAL, false, yyline, yycolumn); }
  
  {Id}			{ return new Token(JminorSym.ID, yytext(), yyline, yycolumn); }
  {Int}			{ try {
                    return new Token(JminorSym.INT_VAL, new Integer(yytext()), yyline, yycolumn);
                   }
                  catch (NumberFormatException e) {
                    throw new LexicalError("Encountered an ill-formatted number: " + yytext(), yyline, yycolumn);
                  } 
                }
  ","  			{ return new Token(JminorSym.COMMA, yytext(), yyline, yycolumn); }
  ";"  			{ return new Token(JminorSym.SEMI, yytext(), yyline, yycolumn); }
  "."  			{ return new Token(JminorSym.DOT, yytext(), yyline, yycolumn); }
  "==" 			{ return new Token(JminorSym.EQ, yytext(), yyline, yycolumn); }
  "!=" 			{ return new Token(JminorSym.NEQ, yytext(), yyline, yycolumn); }
  "<" 			{ return new Token(JminorSym.LT, yytext(), yyline, yycolumn); }
  "<=" 			{ return new Token(JminorSym.LEQ, yytext(), yyline, yycolumn); }
  ">" 			{ return new Token(JminorSym.GT, yytext(), yyline, yycolumn); }
  ">=" 			{ return new Token(JminorSym.GEQ, yytext(), yyline, yycolumn); }
  "=" 			{ return new Token(JminorSym.ASSIGN, yytext(), yyline, yycolumn); }
  
  "&&" 			{ return new Token(JminorSym.AND, yytext(), yyline, yycolumn); }
  "||" 			{ return new Token(JminorSym.OR, yytext(), yyline, yycolumn); }
  "!" 			{ return new Token(JminorSym.NOT, yytext(), yyline, yycolumn); }  
  "+" 			{ return new Token(JminorSym.PLUS, yytext(), yyline, yycolumn); }
  "-" 			{ return new Token(JminorSym.MINUS, yytext(), yyline, yycolumn); }
  "*" 			{ return new Token(JminorSym.TIMES, yytext(), yyline, yycolumn); }
  "/" 			{ return new Token(JminorSym.DIVIDE, yytext(), yyline, yycolumn); }
  
  ":"  			{ return new Token(JminorSym.COLON, yytext(), yyline, yycolumn); }
  "->" 			{ return new Token(JminorSym.ARROW, yytext(), yyline, yycolumn); }
  "..." 		{ return new Token(JminorSym.ELLIPSIS, yytext(), yyline, yycolumn); }  
  "("  			{ return new Token(JminorSym.LP, yytext(), yyline, yycolumn); }
  ")"  			{ return new Token(JminorSym.RP, yytext(), yyline, yycolumn); }
  "{"  			{ return new Token(JminorSym.LCB, yytext(), yyline, yycolumn); }
  "}"  			{ return new Token(JminorSym.RCB, yytext(), yyline, yycolumn); }
  "["  			{ return new Token(JminorSym.LB, yytext(), yyline, yycolumn); }
  "]"  			{ return new Token(JminorSym.RB, yytext(), yyline, yycolumn); }

  {EndOfLineComment}	{ }
  {TraditionalComment}	{ }
  [ \t\n\r] 	{ }
  .   			{ throw new LexicalError("Encountered an illegal character: " + yytext(), yyline, yycolumn); }
}
