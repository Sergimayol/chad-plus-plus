/*
 * Assignatura 21742 - Compiladors
 * Estudis: Grau en Informàtica
 * Itinerari: Computació
 * Curs: 2022 - 2023
 */

package grammar;

import java.io.*;

import java_cup.runtime.*;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import errors.ErrorHandler;
import errors.ErrorCodes;
import utils.Env;

import grammar.ParserSym;

%%

%cup

%public
%class Scanner

%eofval{
  return symbol(ParserSym.EOF);
%eofval}


ID            = [a-zA-Z][a-zA-Z_0-9]*
NUMBER        = 0 | [\+\-]?[1-9][0-9]*
// STRING_LIT    = \" .*? \"               // Optional
COMMENT       = "/*" .*? "*/"
LINE_COMMENT  = "//" ~[\r\n]*
WS            = [ \t]+
ENDLINE       = [\r\n]+


%{
    /*
     * Create ComplexSymbol without attribute
     */
    private ComplexSymbol symbol(int type) {
        return new ComplexSymbol(ParserSym.terminalNames[type], type);
    }

    /*
     * Create ComplexSymbol with attribute
     */
    private ComplexSymbol symbol(int type, Object value) {
        return new ComplexSymbol(ParserSym.terminalNames[type], type, value);
    }
%}


%%

// Rules & actions
// terminals

// reserved key words
"BEGIN"         { return symbol(ParserSym.BEGIN);                 }
"main"          { return symbol(ParserSym.MAIN);                  }
"alpha"         { return symbol(ParserSym.ALPHA);                 }
"const"         { return symbol(ParserSym.CONST);                 }
"return"        { return symbol(ParserSym.RETURN);                }
"true"          { return symbol(ParserSym.VBOL, "true");          }
"false"         { return symbol(ParserSym.VBOL, "false");         }

// types
"int"           { return symbol(ParserSym.INT);                   }
"bol"           { return symbol(ParserSym.BOL);                   }
"tup"           { return symbol(ParserSym.TUP);                   }
"void"          { return symbol(ParserSym.VOID);                  }

// code branching
"if"            { return symbol(ParserSym.IF);                    }
// optional "else"          { return symbol(ParserSym.ELSE);                  }
"while"         { return symbol(ParserSym.WHILE);                 }
"loop"          { return symbol(ParserSym.LOOP);                  }

// logic 2
"&&"            { return symbol(ParserSym.AND);                   }
"||"            { return symbol(ParserSym.OR);                    }
"!"             { return symbol(ParserSym.NOT);                   }

// relational 2
"=="            { return symbol(ParserSym.PLUS);                  }
"<"             { return symbol(ParserSym.MINUS);                 }
">"             { return symbol(ParserSym.MULT);                  }

// arithmetic 2
"+"             { return symbol(ParserSym.PLUS);                  }
"-"             { return symbol(ParserSym.MINUS);                 }
"*"             { return symbol(ParserSym.MULT);                  }
"/"             { return symbol(ParserSym.DIV);                   }

// I/O
"output"        { return symbol(ParserSym.OUT);                   }
"inputint"      { return symbol(ParserSym.ININT);                 }
"inputbol"      { return symbol(ParserSym.INBOL);                 }

// extras
"="             { return symbol(ParserSym.EQUAL);                 }
";"             { return symbol(ParserSym.SEMICOLON);             }
","             { return symbol(ParserSym.COMMA);                 }
"("             { return symbol(ParserSym.LPAREN);                }
")"             { return symbol(ParserSym.RPAREN);                }
"{"             { return symbol(ParserSym.LKEY);                  }
"}"             { return symbol(ParserSym.RKEY);                  }
"["             { return symbol(ParserSym.LSKEY);                 }
"]"             { return symbol(ParserSym.RSKEY);                 }

// non-terminals
{NUMBER}        { return symbol(ParserSym.NUMBER, this.yytext()); }
{ID}            { return symbol(ParserSym.ID, this.yytext());     }

{WS}            {                                                 }
{COMMENT}       {                                                 }
{LINE_COMMENT}  {                                                 }
{ENDLINE}       { return symbol(ParserSym.EOF);                   }

[^]             { ErrorHandler.addError(ErrorCodes.INVALID_TOKEN, yyline, yycolumn, Env.LEXICAL_PHASE);                }
