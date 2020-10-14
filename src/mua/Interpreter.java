package mua;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Interpreter {
    public static void mainLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if ("".equals(line)) {
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

    private static int readLine(String line) {
        String[] literals = line.split("\\s+");
        State state = State.NORMAL;
        ArrayList<String> operationStack = new ArrayList<String>();
        ArrayList<String> valueStack = new ArrayList<String>();
        Map<String, String> variables = new HashMap<String, String>()
        ArrayList<String> basicOperations = new ArrayList<>(Arrays.asList(Operations.BasicOperations));

        for (String literal : literals) {
            System.out.println(literal);

            switch (state) {
                case NORMAL :
                    if (literal.charAt(0) == ':') {
                        //TODO eval the value and put to Stack
                    } 
                    if (basicOperations.contains(literal)) {
                        operationStack.add(literal);
                    } else {
                        valueStack.add(literal);
                    }
                    break;
                case LISBEG :
                    break;
                default :
                    break;
            }
        }

// TODO pop the operations and computes





        return 0;
    }
}
