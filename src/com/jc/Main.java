package com.jc;

import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Templates;

import com.jc.node.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new Exception("You must pass a file to compile. Use the format 'jc <file>'");
        }

        Parser parser = new Parser(args[0]);
        parser.parse();
        Node tree = parser.getTree();
        HashMap<String, String> symbolTable = parser.getSymbolTable();
        HashMap<String, String> tempTable = parser.getTempTable();
        ThreeAddressCodeGenerator codeGenerator = new ThreeAddressCodeGenerator();
        List<Command> code = codeGenerator.generate(tree);

        String layout = "%-5s %-6s %-6s %-6s %-6s\n";
        System.out.format(layout, "label", "op", "right", "left", "dest");
        for (Command command : code) {
            System.out.format(layout, 
                (command.label != null) ? command.label : "", 
                command.op, 
                command.left, 
                command.right, 
                command.result);
        }

        Runner runner = new Runner(code, symbolTable, tempTable);
        runner.run();
    }
}