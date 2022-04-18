package com.jc;

import java_cup.runtime.ComplexSymbolFactory;
import java.lang.*;
import java.io.InputStreamReader;
import java_cup.runtime.Symbol;
import java.io.InputStream;

%%

%class Lexer
%type java_cup.runtime.Symbol
%line
%column

%{

    private ComplexSymbolFactory symbolFactory;

    public Lexer(ComplexSymbolFactory factory, InputStream is) {
        this(is);
        symbolFactory = factory;
    }

    public Symbol symbol(String name, int type) {
        return symbolFactory.newSymbol(name, type);
    }

    public Symbol symbol(String name, int type, Object value) {
        return symbolFactory.newSymbol(name, type, value);
    }

%}

digit       = [0-9]
number      = {digit}+
real        = {digit}+\.{digit}+
bool        = (true|false)
id          = [a-zA-Z_]([a-zA-Z0-9_])*
space       = [ \t\n]
type        = (int|real|bool|str)
string      = \".*\"

%eofval{
    return symbol("EOF", sym.EOF);
%eofval}

%%

{real}          { return symbol("REAL", sym.REAL, Double.valueOf(yytext())); }
{number}        { return symbol("INT", sym.INT, Integer.valueOf(yytext())); }
{bool}          { return symbol("BOOL", sym.BOOL, Boolean.valueOf(yytext())); }
"="             { return symbol("ASSIGN", sym.ASSIGN); }
"**"            { return symbol("POWER", sym.POW); }
"+"             { return symbol("ADD", sym.ADD); }
"-"             { return symbol("SUB", sym.SUB); }
"*"             { return symbol("MULT", sym.MULT); }
"/"             { return symbol("DIV", sym.DIV); }
";"             { return symbol("SEMICOLON", sym.SMC); }
"("             { return symbol("LEFT_PARENTHESIS", sym.L_PTH); }
")"             { return symbol("RIGHT_PARENTHESIS", sym.R_PTH); }
{type}          { return symbol("TYPE", sym.TYPE, yytext()); }
"print"         { return symbol("PRINT", sym.PRINT); }
{string}        { return symbol("STRING", sym.STRING, yytext()); }
{id}            { return symbol("ID", sym.ID, yytext()); }
{space}         { }
.               { System.out.printf("error: unexpected char |%s|\n", yytext()); }

