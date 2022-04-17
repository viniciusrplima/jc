package com.jc;

import java.util.HashMap;
import java.util.List;

import com.jc.node.Expression;
import com.jc.node.Literal;
import com.jc.node.Operation;
import com.jc.node.Variable;


public class Runner {
    
    private List<Command> code;
    private HashMap<String, Integer> labels;
    private HashMap<String, Var> temps;
    private HashMap<String, Var> vars;
    private int currentCommand;

    private class Var {
        public Class<?> type;
        public Object value;
    }

    public Runner(List<Command> code, HashMap<String, String> symbolTable, HashMap<String, String> tempTable) {
        this.code = code;
        this.labels = new HashMap<>();
        this.vars = new HashMap<>();
        this.temps = new HashMap<>();
        
        for (String varName : symbolTable.keySet()) {
            Var var = new Var();
            String type = symbolTable.get(varName);
            
            if (type.equals(Constants.T_REAL)) var.type = Double.class;
            else if (type.equals(Constants.T_INT)) var.type = Integer.class;
            else if (type.equals(Constants.T_BOOL)) var.type = Boolean.class;

            vars.put(varName, var);
        }

        for (String tempName : tempTable.keySet()) {
            Var var = new Var();
            String type = tempTable.get(tempName);
            
            if (type.equals(Constants.T_REAL)) var.type = Double.class;
            else if (type.equals(Constants.T_INT)) var.type = Integer.class;
            else if (type.equals(Constants.T_BOOL)) var.type = Boolean.class;

            temps.put(tempName, var);
        }

        for (int i = 0; i < code.size(); i++) {
            Command command = code.get(i);
            if (command.label != null) {
                labels.put(command.label, i);
            }
        }
    }

    public void run() {
        currentCommand = 0;

        while (currentCommand < code.size()) {
            Command command = code.get(currentCommand);

            if (command.op == Op.OP_STORE) store(command);
            else if (command.op == Op.OP_ADD) add(command);
            else if (command.op == Op.OP_PRINT) print(command);

            currentCommand++;
        }
    }

    private void print(Command command) {
        System.out.println(getValueFromExpression(command.left));
    }

    private void store(Command command) {
        Var variable = vars.get(command.result);
        variable.value = getValueFromExpression(command.left);
    }

    private void add(Command command) {
        Var result = temps.get(command.result);

        if (result.type == Double.class) {
            Double left = getDoubleValueFromExpression(command.left);
            Double right = getDoubleValueFromExpression(command.right);
            result.value = left + right;
        }
        else if (result.type == Integer.class) {
            Integer left = getIntegerValueFromExpression(command.left);
            Integer right = getIntegerValueFromExpression(command.right);
            result.value = left + right;
        }
    }

    private Double getDoubleValueFromExpression(Expression expression) {
        if (expression.type.equals(Constants.T_REAL)) return (Double) getValueFromExpression(expression);
        if (expression.type.equals(Constants.T_INT)) return Double.valueOf((Integer) getValueFromExpression(expression));
        return null;
    }

    private Integer getIntegerValueFromExpression(Expression expression) {
        if (expression.type.equals(Constants.T_REAL)) return (int) Math.floor((Double) getValueFromExpression(expression));
        if (expression.type.equals(Constants.T_INT)) return (Integer) getValueFromExpression(expression);
        return null;
    }

    private Object getValueFromExpression(Expression expression) {
        if (expression instanceof Variable) return vars.get(((Variable) expression).name).value;
        if (expression instanceof Operation) return temps.get(((Operation) expression).temp).value;
        if (expression instanceof Literal) return ((Literal) expression).value;
        return null;
    }
}
