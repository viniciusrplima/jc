package com.jc.interpreter;

import com.jc.node.Expression;

public class Command {
    public String label;
    public Op op;
    public Expression left;
    public Expression right;
    public String result;

    public Command() {
    }

    public Command(Op op, Expression left, Expression right, String result) {
        this.op = op;
        this.left = left;
        this.right = right;
        this.result = result;
    }

    public Command(String label, Op op, Expression left, Expression right, String result) {
        this(op, left, right, result);
        this.label = label;
    }

}