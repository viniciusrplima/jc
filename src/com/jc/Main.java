package com.jc;

import java.util.HashMap;
import java.util.List;

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

        Runner runner = new Runner(code, symbolTable, tempTable);
        runner.run();
    }
}