package mua;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Interpreter {
    static ArrayList<String> unaOps;
    static ArrayList<String> binOps;
    static Map<String, String> variables;
    static Scanner scanner;
    public static void mainLoop() {
        scanner = new Scanner(System.in);
        unaOps = new ArrayList<String>(Arrays.asList(Operations.Operations1));
        binOps = new ArrayList<String>(Arrays.asList(Operations.Operations2));
        variables = new HashMap<String, String>();
        
        while (scanner.hasNext()) {
            //System.out.print("> ");
            String line = scanner.nextLine();
            if ("".equals(line) || line.isEmpty()) {
                continue;
            }
            if (readLine(line) != 0) {
                break;
            }
        }

        scanner.close();
    }

    static enum State {
        NORMAL,
        LISBEG,
    }

    static private Pattern numeric = Pattern.compile("-?\\d+(\\.\\d+)?");
    
    private static int readLine(String line) {
        String[] literals = line.split("\\s+");
        State state = State.NORMAL;
        ArrayList<String> operationStack = new ArrayList<String>();
        ArrayList<String> valueStack = new ArrayList<String>();

        for (String literal : literals) {
            switch (state) {
                case NORMAL :
                    if (literal.charAt(0) == ':') {
                        String value = variables.get(literal.substring(1));
                        if (value.isEmpty()) {
                            System.err.println("Undefined variable: " + literal.substring(1));
                            return 1;
                        }
                        valueStack.add(value);
                    } else if (unaOps.contains(literal) || binOps.contains(literal)) {
                        operationStack.add(literal);
                    } else if (literal.charAt(0) == '"'){
                        valueStack.add(literal.substring(1));
                    } else if (numeric.matcher(literal).matches()) {
                        valueStack.add(literal);
                    }
                    break;
                case LISBEG:
                    break;
                default :
                    break;
            }
        }

        while (!operationStack.isEmpty()) {
            String op = operationStack.remove(operationStack.size() - 1);
            String res;
            if (op.equals("read")) {
                res = scanner.nextLine();
            } else if (op.equals("make")) {
                String val = valueStack.remove(valueStack.size() - 1);
                variables.put(valueStack.remove(valueStack.size() - 1), val);
                res = val;
            }
            else if (unaOps.contains(op)) {
                res = Operations.invoke(op, variables, valueStack.remove(valueStack.size() - 1));
            } else if (binOps.contains(op)) {
                res = Operations.invoke(op, variables, valueStack.remove(valueStack.size() - 2), valueStack.remove(valueStack.size() - 1));
            } else {
                System.err.println("Invalid operator");
                return 1;
            }
            valueStack.add(res);
        }

        //System.out.println(valueStack.get(0));

        return 0;
    }
}
