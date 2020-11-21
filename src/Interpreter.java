package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Interpreter {
    static class Operation {
        String op;
        int operandCnt;

        public Operation(String _op, int _operandCnt) {
            op = _op;
            operandCnt = _operandCnt;
        }

        public Operation(String _op) {
            op = _op;
            operandCnt = binOps.contains(op) ? 2 : unaOps.contains(op) ? 1 : 0;
        }
    }

    static ArrayList<String> unaOps;
    static ArrayList<String> binOps;
    static ArrayList<String> triOps;
    static Map<String, String> variables;
    static ArrayList<Operation> opStack;
    static ArrayList<String> valStack;
    static String buffer;
    static int balanceCnt;
    static int operandCnt;
    public static Scanner scanner;
    static State state;

    public static void mainLoop() throws Exception {
        scanner = new Scanner(System.in);
        unaOps = new ArrayList<>(Arrays.asList(Operations.Operations1));
        binOps = new ArrayList<>(Arrays.asList(Operations.Operations2));
        triOps = new ArrayList<>(Arrays.asList(Operations.Operations3));

        variables = new HashMap<>();

        state = State.NORMAL;
        opStack = new ArrayList<>();
        valStack = new ArrayList<>();
        operandCnt = 0;

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            try {
                readline(line);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        scanner.close();
    }

    public static String readline(String line) throws Exception {
        if (line.isEmpty()) {
            return "";
        }
        String[] literals = line.split("\\s+");
        String tmp = "";
        for (String literal : literals) {
            switch (state) {
                case NORMAL:
                    state = handleLiteral(literal);
                    break;
                case LISBEG:
                    // READ the list and store as "[a,b,[c,d],e]"
                    updateBCnt(literal, '[');
                    if (balanceCnt == 0) {
                        buffer += " " + literal;
                        pushVStack(buffer);
                        state = State.NORMAL;
                    } else {
                        buffer += " " + literal;
                    }
                    break;
                case INFIXBEG:
//                        if (literal.charAt(0) == '(') {
//                            int tmp = balanceCnt;
//                            balanceCnt = 0;
//                            updateBCnt(literal, '(');
//                            if (balanceCnt == 0) {
//                                buffer += eval(literal);
//                                balanceCnt = tmp;
//                                break;
//                            }
//                            balanceCnt = tmp;
//
//                            if (isOp(literal.substring(1))) {
//                                opStack.add(new Operation(literal.substring(1)));
//                                break;
//                            }
//                        }
                    updateBCnt(literal, '(');
                    if (balanceCnt == 0) {
                        buffer += " " + literal;
                        pushVStack(eval(buffer));
                        state = State.NORMAL;
                    } else {
                        buffer += " " + literal;
                    }
                    break;
                case RUN:
                default:
                    break;
            }

            while (!opStack.isEmpty()
                    && opStack.get(opStack.size() - 1).operandCnt == 0) {
                tmp = popStack();
                pushVStack(tmp);
            }
        }
        return tmp;
    }

    enum State {
        NORMAL,
        LISBEG,
        INFIXBEG,
        RUN
    }

    static void pushVStack(String value) {
        if (!opStack.isEmpty()) {
            valStack.add(value);
            opStack.get(opStack.size() - 1).operandCnt--;
        }
    }

    static private final Pattern numeric = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isOp(String name) {
        return name.equals("read") || unaOps.contains(name) || binOps.contains(name) || triOps.contains(name);
    }

    private static int getOperandReq(String op) throws Exception {
        if (op.equals("read")) return 0;
        else if (unaOps.contains(op)) return 1;
        else if (binOps.contains(op)) return 2;
        else if (triOps.contains(op)) return 3;
        else throw new Exception("Invalid Operation: " + op);
    }

    private static void updateBCnt(String literal, char left) throws Exception {
        char right;
        if (left == '(') right = ')';
        else if (left == '[') right = ']';
        else throw new Exception("Unsupported symbol");
        for (int i = 0; i < literal.length(); i++) {
            if (literal.charAt(i) == left) {
                balanceCnt++;
            } else if (literal.charAt(i) == right) {
                if (balanceCnt == 0) throw new Exception("Unbalanced Parenthesis!");
                balanceCnt--;
            }
        }
    }

    private static State handleLiteral(String literal) throws Exception {
        if (isOp(literal)) {
            opStack.add(new Operation(literal, getOperandReq(literal)));
            return State.NORMAL;
        } else if (literal.charAt(0) == '[') {
            buffer = literal;
            balanceCnt = 0;
            updateBCnt(literal, '[');
            if (balanceCnt != 0) {
                return State.LISBEG;
            } else {
                pushVStack(literal);
                return State.NORMAL;
            }
        } else if (literal.charAt(0) == '(') {
            buffer = literal;
            balanceCnt = 0;
            updateBCnt(buffer, '(');
            if (balanceCnt != 0) {
                return State.INFIXBEG;
            } else {
                pushVStack(eval(buffer));
                return State.NORMAL;
            }
        } else if (literal.charAt(0) == ':') { // thing a name
            String value = variables.get(literal.substring(1));
            if (value.isEmpty()) throw new Exception("Undefined variable: " + literal.substring(1));
            pushVStack(value);
        } else if (literal.charAt(0) == '"') { // word
            pushVStack(literal.substring(1));
        } else if (numeric.matcher(literal).matches()) { // number
            pushVStack(literal);
        } else if (literal.equals("true") || literal.equals("false")) {
            pushVStack(literal);
        } else {
            throw new Exception("Invalid literal " + literal);
        }
        return State.NORMAL;
    }

    private static String popStack() throws Exception {
        String op = opStack.get(opStack.size() - 1).op;
        opStack.remove(opStack.size() - 1);
        String res;
        if (op.equals("read")) {
            res = scanner.nextLine();
        } else if (unaOps.contains(op)) {
            res = eval(op, valStack.remove(valStack.size() - 1));
        } else if (binOps.contains(op)) {
            res = eval(op, valStack.remove(valStack.size() - 2), valStack.remove(valStack.size() - 1));
        } else if (triOps.contains(op)) {
            String flag = valStack.remove(valStack.size() - 3);
            if (flag.equals("true")) {
                res = eval("run", valStack.remove(valStack.size() - 2));
            } else if (flag.equals("false")) {
                res = eval("run", valStack.remove(valStack.size() - 1));
            } else {
                throw new Exception("if not accepting legal operands. flag = " + flag );
            }
            valStack.remove(valStack.size() - 1);
        } else {
            throw new Exception("Invalid operator" + op);
        }

        return res;
    }

    private static String eval(String op, String var1) throws Exception {
        return Operations.invoke(op, variables, var1);
    }

    private static String eval(String op, String var1, String var2) throws Exception {
        return Operations.invoke(op, variables, var1, var2);
    }

    private static String eval(String expression) throws Exception {
        ArrayList<String> operands = new ArrayList<>();
        ArrayList<String> operators = new ArrayList<>();
        String op0 = "()";
        String op3 = ":";
        String op2 = "*/%";
        String op1 = "+-";
        String op = op0 + op1 + op2 + op3;
        Map<String, Integer> priority = new HashMap<>();
        priority.put("+", 1);
        priority.put("-", 1);
        priority.put("*", 2);
        priority.put("/", 2);
        priority.put("%", 2);
        priority.put(":", 3);
        priority.put("(", 0);
        priority.put(")", 0);
        priority.put("add", 1);

        String state = "init";
        double operand;
        for (int i = 0; i < expression.length(); i++) {
            char cur = expression.charAt(i);
            if (cur == '-' && (state.equals("op") || (i > 0 && expression.charAt(i - 1) == '(')) || cur >= '0' && cur <= '9') {
                if (cur != '-') operand = cur - '0';
                else {
                    i++;
                    cur = expression.charAt(i);
                    operand = (double) -1 * (cur - '0');
                }
                while (i + 1 < expression.length()) {
                    cur = expression.charAt(i + 1);
                    if (!(cur >= '0' && cur <='9')) break;
                    operand = operand * 10 + (cur - '0');
                    i++;
                }
                operands.add(Double.toString(operand));
                state = "num";
            } else if (op.contains(String.valueOf(cur))) {
                if (cur == '(' || operators.isEmpty() || priority.get(String.valueOf(cur)) >= priority.get(operators.get(operators.size() - 1))) {
                    operators.add(String.valueOf(cur));
                } else {
                    cleanStack(operands, operators);
                    if (cur == ')' && operators.get(operators.size() - 1).equals("(")) {
                        operators.remove(operators.size() - 1);
                    } else {
                        operators.add(String.valueOf(cur));
                    }
                }
                state = "op";
            } else if (Character.isLetter(cur)) {
                StringBuilder name = new StringBuilder(String.valueOf(cur));
                while (i + 1 < expression.length() && Character.isLetterOrDigit(expression.charAt(i + 1) )) {
                    name.append(expression.charAt(i + 1));
                    i++;
                }
                String literal = name.toString();
                if (binOps.contains(literal) || unaOps.contains(literal)) {
                    operators.add(literal);
                } else {
                    operands.add(name.toString());
                }
            }
        }
        return operands.get(0);
    }

    private static void cleanStack(ArrayList<String> operands, ArrayList<String> operators) throws Exception {
        while (!operators.isEmpty() && !operators.get(operators.size() - 1).equals("(")) {
            String Op = operators.remove(operators.size() - 1);
            double res = switch (Op) {
                case "+", "add" -> Double.parseDouble(operands.remove(operands.size() - 2)) + Double.parseDouble(operands.remove(operands.size() - 1));
                case "-" -> Double.parseDouble(operands.remove(operands.size() - 2)) - Double.parseDouble(operands.remove(operands.size() - 1));
                case "*" -> Double.parseDouble(operands.remove(operands.size() - 2)) * Double.parseDouble(operands.remove(operands.size() - 1));
                case "/" -> Double.parseDouble(operands.remove(operands.size() - 2)) / Double.parseDouble(operands.remove(operands.size() - 1));
                case "%" -> Double.parseDouble(operands.remove(operands.size() - 2)) % Double.parseDouble(operands.remove(operands.size() - 1));
                case ":" -> Double.parseDouble(variables.get(operands.remove(operands.size() - 1)));
                default -> throw new Exception("Invalid Operator");
            };
            operands.add(Double.toString(res));
        }
    }
}
