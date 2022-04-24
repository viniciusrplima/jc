package com.jc.interpreter;

import java.util.ArrayList;
import java.util.List;

import com.jc.node.*;

import static com.jc.Constants.*;

public class ThreeAddressCodeGenerator {

    private int labelSequence;

    public ThreeAddressCodeGenerator() {
        labelSequence = 1;
    }
    
    public List<Command> generate(Node tree) {
        String nextLabel = newLabel();
        List<Command> code = new ArrayList<>();
        
        for (Node node : tree.children) {
            code.addAll(generateForSubtree(node, nextLabel));
        }

        code.add(newLabelCommand(nextLabel));
        return code;
    }

    private String newLabel() {
        return "L" + labelSequence++;
    }

    private Command newLabelCommand(String label) {
        return new Command(label, null, null, null, null);
    }

    private List<Command> generateForSubtree(Node tree, String nextLabel) {
        if (tree instanceof Declaration) return new ArrayList<>();
        else if (tree instanceof Assignment) return generateCommands((Assignment) tree);
        else if (tree instanceof Increment) return generateCommands((Increment) tree);
        else if (tree instanceof Operation) return generateCommands((Operation) tree, nextLabel);
        else if (tree instanceof Conditional) return generateCommands((Conditional) tree, nextLabel);
        else if (tree instanceof WhileLoop) return generateCommands((WhileLoop) tree, nextLabel);
        else if (tree instanceof DoWhileLoop) return generateCommands((DoWhileLoop) tree, nextLabel);
        else if (tree instanceof ForLoop) return generateCommands((ForLoop) tree, nextLabel);
        else if (tree instanceof Print) return generateCommands((Print) tree);
        else if (tree instanceof Expression) return new ArrayList<>();
        else if (tree instanceof Node) return generateCommands(tree);
        return new ArrayList<>();
    }

    private List<Command> generateCommands(Node tree) {
        List<Command> code = new ArrayList<>();
        String nextLabel = newLabel();

        if (tree.children != null) {
            for (Node node : tree.children) {
                code.addAll(generateForSubtree(node, nextLabel));
            }
    
            code.add(newLabelCommand(nextLabel));
        }

        return code;
    }

    private List<Command> generateCommands(Increment increment) {
        List<Command> code = new ArrayList<>();

        Op op = null;
        if (increment.operation.equals(OP_INC)) op = Op.OP_INC;
        else if (increment.operation.equals(OP_DEC)) op = Op.OP_DEC;

        Expression variableExp = (Expression) increment.children.get(0);

        code.add(new Command(op, variableExp, null, increment.temp));
        code.add(new Command(Op.OP_STORE, increment, null, increment.variable));

        return code;
    }

    private List<Command> generateCommands(WhileLoop whileLoop, String nextLabel) {
        String childIfFalseLabel = newLabel();
        String testLabel = newLabel();
        String testNextLabel = newLabel();
        
        List<Command> code = new ArrayList<>();
        code.add(newLabelCommand(testLabel));
        code.addAll(generateForSubtree(whileLoop.test, testNextLabel));
        code.add(newLabelCommand(testNextLabel));
        code.add(new Command(Op.OP_IFFALSE_JUMP, whileLoop.test, null, childIfFalseLabel));
        code.addAll(generateForSubtree(whileLoop.children.get(0), nextLabel));
        code.add(new Command(Op.OP_JUMP, null, null, testLabel));
        code.add(newLabelCommand(childIfFalseLabel));

        return code;
    }

    private List<Command> generateCommands(ForLoop forLoop, String nextLabel) {
        String ifFalseLabel = newLabel();
        String testLabel = newLabel();
        String testNextLabel = newLabel();
        String incrementNextLabel = newLabel();

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(forLoop.declaration, null));
        code.add(newLabelCommand(testLabel));
        code.addAll(generateForSubtree(forLoop.test, testNextLabel));
        code.add(newLabelCommand(testNextLabel));
        code.add(new Command(Op.OP_IFFALSE_JUMP, forLoop.test, null, ifFalseLabel));
        code.addAll(generateForSubtree(forLoop.children.get(0), nextLabel));
        code.add(newLabelCommand(nextLabel));
        code.addAll(generateForSubtree(forLoop.increment, incrementNextLabel));
        code.add(newLabelCommand(incrementNextLabel));
        code.add(new Command(Op.OP_JUMP, null, null, testLabel));
        code.add(newLabelCommand(ifFalseLabel));

        return code;
    }

    private List<Command> generateCommands(DoWhileLoop doWhileLoop, String nextLabel) {
        String ifTrueLabel = newLabel();
        String testNextLabel = newLabel();
        
        List<Command> code = new ArrayList<>();
        code.add(newLabelCommand(ifTrueLabel));
        code.addAll(generateForSubtree(doWhileLoop.children.get(0), nextLabel));
        code.addAll(generateForSubtree(doWhileLoop.test, testNextLabel));
        code.add(newLabelCommand(testNextLabel));
        code.add(new Command(Op.OP_IFTRUE_JUMP, doWhileLoop.test, null, ifTrueLabel));

        return code;
    }

    private List<Command> generateCommands(Conditional conditional, String nextLabel) {
        String ifFalseLabel = newLabel();
        String testNextLabel = newLabel();

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(conditional.test, testNextLabel));
        code.add(newLabelCommand(testNextLabel));
        code.add(new Command(Op.OP_IFFALSE_JUMP, conditional.test, null, ifFalseLabel));
        code.addAll(generateForSubtree(conditional.children.get(0), nextLabel));
        code.add(new Command(Op.OP_JUMP, null, null, nextLabel));
        code.add(newLabelCommand(ifFalseLabel));

        if (conditional.children.size() > 1) {
            code.addAll(generateForSubtree(conditional.children.get(1), nextLabel));
        }

        return code;
    }

    private List<Command> generateCommands(Print print) {
        String childNextLabel = newLabel();

        Expression expression = (Expression) print.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, childNextLabel));
        code.add(newLabelCommand(childNextLabel));
        code.add(new Command(Op.OP_PRINT, expression, null, null));
        return code;
    }

    private List<Command> generateCommands(Assignment assignment) {
        String childNextLabel = newLabel();

        Expression expression = (Expression) assignment.children.get(0);

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(expression, childNextLabel));
        code.add(newLabelCommand(childNextLabel));
        code.add(new Command(Op.OP_STORE, expression, null, assignment.variable));
        return code;
    }

    private List<Command> generateCommands(Operation operation, String nextLabel) {
        Op op = null;
        if (operation.operation.equals(OP_ADD)) op = Op.OP_ADD;
        if (operation.operation.equals(OP_SUB)) op = Op.OP_SUB;
        if (operation.operation.equals(OP_MULT)) op = Op.OP_MULT;
        if (operation.operation.equals(OP_DIV)) op = Op.OP_DIV;
        if (operation.operation.equals(OP_POW)) op = Op.OP_POW;
        if (operation.operation.equals(OP_OR)) op = Op.OP_OR;
        if (operation.operation.equals(OP_AND)) op = Op.OP_AND;
        if (operation.operation.equals(OP_NOT)) op = Op.OP_NOT;
        if (operation.operation.equals(OP_EQ)) op = Op.OP_EQ;
        if (operation.operation.equals(OP_NEQ)) op = Op.OP_NEQ;
        if (operation.operation.equals(OP_GT)) op = Op.OP_GT;
        if (operation.operation.equals(OP_GTE)) op = Op.OP_GTE;
        if (operation.operation.equals(OP_LT)) op = Op.OP_LT;
        if (operation.operation.equals(OP_LTE)) op = Op.OP_LTE;

        Expression left = (Expression) operation.children.get(0);
        Expression right = null;
        if (operation.children.size() > 1) {
            right = (Expression) operation.children.get(1);
        }

        if (op == Op.OP_OR) return generateOrOperationCommands(operation, op, left, right, nextLabel);
        else if (op == Op.OP_AND) return generateAndOperationCommands(operation, op, left, right, nextLabel);
        else return generateStandardOperationCommands(operation, op, left, right);

    }

    public List<Command> generateAndOperationCommands(
        Operation operation, Op op, Expression left, Expression right, String nextLabel) {
        
        String leftNextLabel = newLabel();
        String leftIfTrueLabel = leftNextLabel;
        String rightNextLabel = newLabel();

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftNextLabel));
        code.add(new Command(Op.OP_IFTRUE_JUMP, left, null, leftIfTrueLabel));
        code.add(new Command(Op.OP_MOV, left, null, operation.temp));
        code.add(new Command(Op.OP_JUMP, null, null, nextLabel));
        code.add(newLabelCommand(leftNextLabel));
        code.addAll(generateForSubtree(right, rightNextLabel));
        code.add(newLabelCommand(rightNextLabel));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

    public List<Command> generateOrOperationCommands(
        Operation operation, Op op, Expression left, Expression right, String nextLabel) {
        
        String leftNextLabel = newLabel();
        String rightNextLabel = newLabel();
        String leftIfFalseLabel = leftNextLabel;
        
        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftNextLabel));
        code.add(new Command(Op.OP_IFFALSE_JUMP, left, null, leftIfFalseLabel));
        code.add(new Command(Op.OP_MOV, left, null, operation.temp));
        code.add(new Command(Op.OP_JUMP, null, null, nextLabel));
        code.add(newLabelCommand(leftNextLabel));
        code.addAll(generateForSubtree(right, rightNextLabel));
        code.add(newLabelCommand(rightNextLabel));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

    public List<Command> generateStandardOperationCommands(
        Operation operation, Op op, Expression left, Expression right) {

        String leftNextLabel = newLabel();
        String rightNextLabel = newLabel();

        List<Command> code = new ArrayList<>();
        code.addAll(generateForSubtree(left, leftNextLabel));
        code.add(newLabelCommand(leftNextLabel));
        code.addAll(generateForSubtree(right, rightNextLabel));
        code.add(newLabelCommand(rightNextLabel));
        code.add(new Command(op, left, right, operation.temp));

        return code;
    }

}
