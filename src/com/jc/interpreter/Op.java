package com.jc.interpreter;

public enum Op {
    OP_STORE, 
    OP_LOAD, 
    OP_MOV, 
    OP_ADD, 
    OP_SUB, 
    OP_MULT, 
    OP_DIV, 
    OP_POW, 
    OP_DEC, 
    OP_INC, 
    OP_PRINT, 
    OP_OR, 
    OP_AND, 
    OP_NOT, 
    OP_EQ, 
    OP_NEQ, 
    OP_GT, 
    OP_GTE, 
    OP_LT, 
    OP_LTE, 
    OP_JUMP, 
    OP_IFTRUE_JUMP, 
    OP_IFFALSE_JUMP;
}
