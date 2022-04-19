package com.jc;

import java.util.ArrayList;
import java.util.List;

import com.jc.node.*;

import static com.jc.Constants.*;

public class ThreeAddressCodeGenerator {

    private int labelSequence;

    private class Block {
        public String next;
        public String ifTrue;
        public String ifFalse;
        public List<Command> code;
    }

    public ThreeAddressCodeGenerator() {
        labelSequence = 1;
    }
    
    public List<Command> generate(Node tree) {
        Block program = new Block();
        program.next = newLabel();
        program.code = new ArrayList<>();
        
        for (Node node : tree.children) {
            program.code.addAll(generateForSubtree(node, program));
        }

        Command lastLabel = new Command();
        lastLabel.label = program.next;
        program.code.add(lastLabel);
        return program.code;
    }

    private String newLabel() {
        return "L" + labelSequence++;
    }

    private List<Command> generateForSubtree(Node tree, Block parent) {
        if (tree instanceof Declaration) return new ArrayList<>();
        else if (tree instanceof Assignment) return generateCommands((Assignment) tree);
        else if (tree instanceof Operation) return generateCommands((Operation) tree);
        else if (tree instanceof Print) return generateCommands((Print) tree);
        return new ArrayList<>();
    }

    private List<Command> generateCommands(Print print) {
        Expression expression = (Expression) print.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, null));
        code.add(new Command(Op.OP_PRINT, expression, null, null));
        return code;
    }

    private List<Command> generateCommands(Assignment assignment) {
        Expression expression = (Expression) assignment.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, null));
        code.add(new Command(Op.OP_STORE, expression, null, assignment.variable));
        return code;
    }

    private List<Command> generateCommands(Operation operation) {
        Op op = null;
        if (operation.operation.equals(OP_ADD)) op = Op.OP_ADD;
        if (operation.operation.equals(OP_SUB)) op = Op.OP_SUB;
        if (operation.operation.equals(OP_MULT)) op = Op.OP_MULT;
        if (operation.operation.equals(OP_DIV)) op = Op.OP_DIV;
        if (operation.operation.equals(OP_POW)) op = Op.OP_POW;
        if (operation.operation.equals(OP_OR)) op = Op.OP_OR;
        if (operation.operation.equals(OP_AND)) op = Op.OP_AND;
        if (operation.operation.equals(OP_NOT)) op = Op.OP_NOT;

        Expression left = (Expression) operation.children.get(0);
        Expression right = null;
        if (operation.children.size() > 1) {
            right = (Expression) operation.children.get(1);
        }

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, null));
        code.addAll(generateForSubtree(right, null));
        code.add(new Command(op, left, right, operation.temp));
        return code;
    }

}
