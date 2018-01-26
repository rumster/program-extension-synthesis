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
%scanerror LexicalError

%{
StringBuffer string = new StringBuffer();

public int getLineNumber() { return yyline + 1; }
%}

%eofval{
  return new Token(HeapSym.EOF, yytext(), yyline, yycolumn);
%eofval}

Letter 		= [a-zA-Z]
Digit 		= [0-9]
PosDigit	= [1-9]
Id 			= ({Letter}|_)({Letter}|_|{Digit})*
Int			= 0|{PosDigit}{Digit}*|-{PosDigit}{Digit}*

%state COMMENT
%state LINECOMMENT

%%

<YYINITIAL> {
  "type"		{ return new Token(HeapSym.TYPE, yytext(), yyline, yycolumn); }
  "example"		{ return new Token(HeapSym.EXAMPLE, yytext(), yyline, yycolumn); }
  "var"			{ return new Token(HeapSym.VAR, yytext(), yyline, yycolumn); }
  "null"		{ return new Token(HeapSym.NULL, yytext(), yyline, yycolumn); }
  "mut"			{ return new Token(HeapSym.MUT, yytext(), yyline, yycolumn); }
  "ghost"		{ return new Token(HeapSym.GHOST, yytext(), yyline, yycolumn); }
  
  {Id}			{ return new Token(HeapSym.ID, yytext(), yyline, yycolumn); }
  {Int}			{ try {
                    return new Token(HeapSym.INT_VAL, new Integer(yytext()), yyline, yycolumn);
                   }
                  catch (NumberFormatException e) {
                    throw new LexicalError("Encountered an ill-formatted number: " + yytext() + " at " + yyline + ":" + yycolumn);
                  } 
                }
  ","  			{ return new Token(HeapSym.COMMA, yytext(), yyline, yycolumn); }
  "."  			{ return new Token(HeapSym.DOT, yytext(), yyline, yycolumn); }
  "==" 			{ return new Token(HeapSym.EQ, yytext(), yyline, yycolumn); }
  "=" 			{ return new Token(HeapSym.ASSIGN, yytext(), yyline, yycolumn); }
  "&&" 			{ return new Token(HeapSym.AND, yytext(), yyline, yycolumn); }
  ":"  			{ return new Token(HeapSym.COLON, yytext(), yyline, yycolumn); }
  "->" 			{ return new Token(HeapSym.ARROW, yytext(), yyline, yycolumn); }
  "("  			{ return new Token(HeapSym.LP, yytext(), yyline, yycolumn); }
  ")"  			{ return new Token(HeapSym.RP, yytext(), yyline, yycolumn); }
  "{"  			{ return new Token(HeapSym.LCB, yytext(), yyline, yycolumn); }
  "}"  			{ return new Token(HeapSym.RCB, yytext(), yyline, yycolumn); }

  "//"			{ yybegin(LINECOMMENT); }
  [ \t\n\r] 	{ }
  .   			{ throw new LexicalError("Encountered an illegal character: " + yytext() + " at " + yyline + ":" + yycolumn); }
}

/////////////////////////////////////////////////////////////////////////////
// Comments
/////////////////////////////////////////////////////////////////////////////

<LINECOMMENT>\r 		{ }
<LINECOMMENT>\n  		{ yybegin(YYINITIAL); }
<LINECOMMENT>.      	{ }