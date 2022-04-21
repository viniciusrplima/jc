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

    private Command newLabelCommand(String label) {
        return new Command(label, null, null, null, null);
    }

    private List<Command> generateForSubtree(Node tree, Block parent) {
        if (tree instanceof Declaration) return new ArrayList<>();
        else if (tree instanceof Assignment) return generateCommands((Assignment) tree);
        else if (tree instanceof Operation) return generateCommands((Operation) tree, parent);
        else if (tree instanceof Conditional) return generateCommands((Conditional) tree, parent);
        else if (tree instanceof Print) return generateCommands((Print) tree);
        else if (tree instanceof Expression) return new ArrayList<>();
        else if (tree instanceof Node) return generateCommands(tree);
        return new ArrayList<>();
    }

    private List<Command> generateCommands(Node tree) {
        List<Command> code = new ArrayList<>();
        Block child = new Block();
        child.next = newLabel();

        if (tree.children != null) {
            for (Node node : tree.children) {
                code.addAll(generateForSubtree(node, child));
            }
    
            code.add(newLabelCommand(child.next));
        }

        return code;
    }

    private List<Command> generateCommands(Conditional conditional, Block parent) {
        Block child = new Block();
        child.next = parent.next;
        child.ifFalse = newLabel();
        
        List<Command> code = new ArrayList<>();
        code.add(new Command(Op.OP_IFFALSE_JUMP, conditional.test, null, child.ifFalse));
        code.addAll(generateForSubtree(conditional.children.get(0), child));
        code.add(new Command(Op.OP_JUMP, null, null, parent.next));
        code.add(newLabelCommand(child.ifFalse));

        if (conditional.children.size() > 1) {
            code.addAll(generateForSubtree(conditional.children.get(1), parent));
        }

        return code;
    }

    private List<Command> generateCommands(Print print) {
        Block child = new Block();
        child.next = newLabel();
        Expression expression = (Expression) print.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, child));
        code.add(new Command(child.next, null, null, null, null));
        code.add(new Command(Op.OP_PRINT, expression, null, null));
        return code;
    }

    private List<Command> generateCommands(Assignment assignment) {
        Block child = new Block();
        child.next = newLabel();
        Expression expression = (Expression) assignment.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, child));
        code.add(new Command(child.next, null, null, null, null));
        code.add(new Command(Op.OP_STORE, expression, null, assignment.variable));
        return code;
    }

    private List<Command> generateCommands(Operation operation, Block parent) {
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

        if (op == Op.OP_OR) return generateOrOperationCommands(operation, op, left, right, parent);
        else if (op == Op.OP_AND) return generateAndOperationCommands(operation, op, left, right, parent);
        else return generateStandardOperationCommands(operation, op, left, right, parent);

    }

    public List<Command> generateAndOperationCommands(
        Operation operation, Op op, Expression left, Expression right, Block parent) {
        
        Block leftBlock = new Block();
        Block rightBlock = new Block();
        rightBlock.next = newLabel();
        rightBlock.ifTrue = rightBlock.next;
        rightBlock.ifFalse = rightBlock.next;
        leftBlock.next = newLabel();
        leftBlock.ifFalse = rightBlock.next;
        leftBlock.ifTrue = leftBlock.next;

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftBlock));
        code.add(new Command(Op.OP_IFTRUE_JUMP, left, null, leftBlock.ifTrue));
        code.add(new Command(Op.OP_MOV, left, null, operation.temp));
        code.add(new Command(Op.OP_JUMP, null, null, parent.next));
        code.add(newLabelCommand(leftBlock.next));
        code.addAll(generateForSubtree(right, rightBlock));
        code.add(newLabelCommand(rightBlock.next));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

    public List<Command> generateOrOperationCommands(
        Operation operation, Op op, Expression left, Expression right, Block parent) {
        
        Block leftBlock = new Block();
        Block rightBlock = new Block();
        rightBlock.next = newLabel();
        rightBlock.ifTrue = rightBlock.next;
        rightBlock.ifFalse = rightBlock.next;
        leftBlock.next = newLabel();
        leftBlock.ifFalse = leftBlock.next;
        leftBlock.ifTrue = rightBlock.next;

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftBlock));
        code.add(new Command(Op.OP_IFFALSE_JUMP, left, null, leftBlock.ifFalse));
        code.add(new Command(Op.OP_MOV, left, null, operation.temp));
        code.add(new Command(Op.OP_JUMP, null, null, parent.next));
        code.add(newLabelCommand(leftBlock.next));
        code.addAll(generateForSubtree(right, rightBlock));
        code.add(newLabelCommand(rightBlock.next));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

    public List<Command> generateStandardOperationCommands(
        Operation operation, Op op, Expression left, Expression right, Block parent) {

        Block leftBlock = new Block();
        Block rightBlock = new Block();
        leftBlock.ifFalse = newLabel();
        leftBlock.ifTrue = newLabel();
        leftBlock.next = newLabel();
        rightBlock.ifTrue = newLabel();
        rightBlock.ifFalse = newLabel();
        rightBlock.next = newLabel();

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftBlock));
        code.add(newLabelCommand(leftBlock.next));
        code.addAll(generateForSubtree(right, rightBlock));
        code.add(newLabelCommand(rightBlock.next));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

}
