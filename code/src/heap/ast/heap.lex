// @author Roman Manevich
package heap.ast;

/** A scanner for heap synthesis problems.
 * @author Roman Manevich
 */
%%

%class HeapLexer
%cupsym HeapSym
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
  return new Token(HeapSym.EOF, yytext(), yyline, yycolumn);
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
  "type"		{ return new Token(HeapSym.TYPE, yytext(), yyline, yycolumn); }
  "example"		{ return new Token(HeapSym.EXAMPLE, yytext(), yyline, yycolumn); }
  "var"			{ return new Token(HeapSym.VAR, yytext(), yyline, yycolumn); }
  "null"		{ return new Token(HeapSym.NULL, yytext(), yyline, yycolumn); }
  "mut"			{ return new Token(HeapSym.MUT, yytext(), yyline, yycolumn); }
  "ghost"		{ return new Token(HeapSym.GHOST, yytext(), yyline, yycolumn); }

  "if"			{ return new Token(HeapSym.IF, yytext(), yyline, yycolumn); }
  "else"		{ return new Token(HeapSym.ELSE, yytext(), yyline, yycolumn); }  
  "while"		{ return new Token(HeapSym.WHILE, yytext(), yyline, yycolumn); }
  
  {Id}			{ return new Token(HeapSym.ID, yytext(), yyline, yycolumn); }
  {Int}			{ try {
                    return new Token(HeapSym.INT_VAL, new Integer(yytext()), yyline, yycolumn);
                   }
                  catch (NumberFormatException e) {
                    throw new LexicalError("Encountered an ill-formatted number: " + yytext(), yyline, yycolumn);
                  } 
                }
  ","  			{ return new Token(HeapSym.COMMA, yytext(), yyline, yycolumn); }
  ";"  			{ return new Token(HeapSym.SEMI, yytext(), yyline, yycolumn); }
  "."  			{ return new Token(HeapSym.DOT, yytext(), yyline, yycolumn); }
  "==" 			{ return new Token(HeapSym.EQ, yytext(), yyline, yycolumn); }
  "!=" 			{ return new Token(HeapSym.NEQ, yytext(), yyline, yycolumn); }
  "<" 			{ return new Token(HeapSym.LT, yytext(), yyline, yycolumn); }
  "<=" 			{ return new Token(HeapSym.LEQ, yytext(), yyline, yycolumn); }
  ">" 			{ return new Token(HeapSym.GT, yytext(), yyline, yycolumn); }
  ">=" 			{ return new Token(HeapSym.GEQ, yytext(), yyline, yycolumn); }
  "=" 			{ return new Token(HeapSym.ASSIGN, yytext(), yyline, yycolumn); }
  
  "&&" 			{ return new Token(HeapSym.AND, yytext(), yyline, yycolumn); }
  "||" 			{ return new Token(HeapSym.OR, yytext(), yyline, yycolumn); }
  "!" 			{ return new Token(HeapSym.NOT, yytext(), yyline, yycolumn); }  
  "+" 			{ return new Token(HeapSym.PLUS, yytext(), yyline, yycolumn); }
  "-" 			{ return new Token(HeapSym.MINUS, yytext(), yyline, yycolumn); }
  "*" 			{ return new Token(HeapSym.TIMES, yytext(), yyline, yycolumn); }
  "/" 			{ return new Token(HeapSym.DIVIDE, yytext(), yyline, yycolumn); }
  
  ":"  			{ return new Token(HeapSym.COLON, yytext(), yyline, yycolumn); }
  "->" 			{ return new Token(HeapSym.ARROW, yytext(), yyline, yycolumn); }
  "("  			{ return new Token(HeapSym.LP, yytext(), yyline, yycolumn); }
  ")"  			{ return new Token(HeapSym.RP, yytext(), yyline, yycolumn); }
  "{"  			{ return new Token(HeapSym.LCB, yytext(), yyline, yycolumn); }
  "}"  			{ return new Token(HeapSym.RCB, yytext(), yyline, yycolumn); }
  "["  			{ return new Token(HeapSym.LB, yytext(), yyline, yycolumn); }
  "]"  			{ return new Token(HeapSym.RB, yytext(), yyline, yycolumn); }

  {EndOfLineComment}	{ }
  {TraditionalComment}	{ }
  [ \t\n\r] 	{ }
  .   			{ throw new LexicalError("Encountered an illegal character: " + yytext(), yyline, yycolumn); }
}
