package mua;

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

    enum State {
        NORMAL,
        LISBEG,
        INFIXBEG,
        RUN
    }

    static ArrayList<String> immOps;
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
    static ArrayList<Map<String, String>> localStack;
    static boolean isReturning = false;

    static void initialize() throws Exception {
        eval("make", "pi", "3.14159");
    }

    public static void mainLoop() throws Exception {
        scanner = new Scanner(System.in);
        immOps = new ArrayList<>(Arrays.asList(Operations.Operations0));
        unaOps = new ArrayList<>(Arrays.asList(Operations.Operations1));
        binOps = new ArrayList<>(Arrays.asList(Operations.Operations2));
        triOps = new ArrayList<>(Arrays.asList(Operations.Operations3));

        variables = new HashMap<>();
        localStack = new ArrayList<>();

        state = State.NORMAL;
        opStack = new ArrayList<>();
        valStack = new ArrayList<>();
        operandCnt = 0;

        initialize();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            try {
                readline(line);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        scanner.close();
    }

    public static String readline(String line) throws Exception {
        if (line.isEmpty() || line.equals("")) {
            return "";
        }
        String[] literals = line.split("\\s+");
        String tmp = "";
        for (String literal : literals) {
            if (literal.equals("")) continue;
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

            while (!opStack.isEmpty()) {
                Operation top = opStack.get(opStack.size() - 1);
                if (top.op.equals("__FENCE__") || top.operandCnt != 0) break;
                if (top.op.equals("return")) {
                    tmp = popStack();
                    isReturning = true;
                    while (!opStack.isEmpty() && !opStack.get(opStack.size() - 1).op.equals("__FENCE__")) {
                        opStack.remove(opStack.size() - 1);
                    }
                    while (!valStack.isEmpty() && !valStack.get(valStack.size() - 1).equals("__FENCE__")) {
                        valStack.remove(valStack.size() - 1);
                    }
                    return tmp;
                }
                tmp = popStack();
                if (isReturning) {
                    return tmp;
                };
                pushVStack(tmp);
            }
        }
        return tmp;
    }

    static void pushVStack(String value) {
        if (!opStack.isEmpty() && !opStack.get(opStack.size() - 1).op.equals("__FENCE__")) {
            valStack.add(value);
            opStack.get(opStack.size() - 1).operandCnt--;
        }
    }

    private static String popStack() throws Exception {
        String op = opStack.get(opStack.size() - 1).op;

        String res;
        if (op.equals("read")) {
            res = scanner.nextLine();
        } else if (op.equals("readlist")) {
            res = "[" + scanner.nextLine().replaceAll("(^ )|( $)", "") + "]";
        } else if (immOps.contains(op)) {
            res = Operations.invoke(op);
        } else if (unaOps.contains(op)) {
            res = eval(op, valStack.remove(valStack.size() - 1));
        } else if (binOps.contains(op)) {
            res = eval(op, valStack.remove(valStack.size() - 2), valStack.remove(valStack.size() - 1));
        } else if (triOps.contains(op)) {
            String flag = valStack.remove(valStack.size() - 3);
            if (flag.equals("true")) {
                valStack.remove(valStack.size() - 1);
                res = eval("run", valStack.remove(valStack.size() - 1));
            } else if (flag.equals("false")) {
                valStack.remove(valStack.size() - 2);
                res = eval("run", valStack.remove(valStack.size() - 1));
            } else {
                throw new Exception("if not accepting legal operands. flag = " + flag);
            }
        } else if (!localStack.isEmpty() && localStack.get(localStack.size() - 1).containsKey(op)) {
            res = callSubProgram(op, valStack);
        } else if (variables.containsKey(op)) {
            res = callSubProgram(op, valStack);
        } else if (op.equals("__FENCE__")) {
            throw new Exception("Popping fence!");
        } else {
            throw new Exception("Invalid operator" + op);
        }
        if (!isReturning) opStack.remove(opStack.size() - 1);
        return res;
    }

    static String callSubProgram (String op, ArrayList<String> vals) throws Exception {
        Map<String, String> args = new HashMap<>();
        String[] argList = getArgs(op);
        for (int i = argList.length - 1; i >= 0; i--) {
            args.put(argList[i], vals.remove(vals.size() - 1));
        }
        boolean lastReturnStatus = isReturning;
        State lastState = state;
        state = State.NORMAL;
        valStack.add("__FENCE__");
        opStack.add(new Operation("__FENCE__", -1));
        String body = getBody(op);
        localStack.add(args);
        String res = eval("run", body);
        localStack.remove(localStack.size() - 1);
        isReturning = lastReturnStatus;
        state = lastState;
//        while (!opStack.isEmpty() && !opStack.get(opStack.size() - 1).op.equals("__FENCE__")) {
//            opStack.remove(opStack.size() - 1);
//        }
//        while (!valStack.isEmpty() && !valStack.get(valStack.size() - 1).equals("__FENCE__")) {
//            valStack.remove(valStack.size() - 1);
//        }
        opStack.remove(opStack.size() - 1);
        valStack.remove(valStack.size() - 1);
        return res;
    }

    static private final Pattern numeric = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isOp(String name) {
        return immOps.contains(name)|| unaOps.contains(name) || binOps.contains(name) || triOps.contains(name);
    }

    private static int getOperandReq(String op) throws Exception {
        if (immOps.contains(op)) return 0;
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
    private static String getBody(String op) {
        String body;
        if (!localStack.isEmpty() && localStack.get(localStack.size() - 1).containsKey(op)) {
            body = localStack.get(localStack.size() - 1).get(op);
        } else {
            body = variables.get(op);
        }
        body = body.substring(1, body.length() - 1);
        // System.out.println(body.split("[\\[\\]]", 3)[2].strip());
        return body.split("[\\[\\]]", 3)[2].replaceAll("(^ )|( $)", ""); // TODO to be checked
    }
    private static String[] getArgs(String op) {
        String[] res;
        if (!localStack.isEmpty() && localStack.get(localStack.size() - 1).containsKey(op)) {
            res = localStack.get(localStack.size() - 1).get(op).split("[\\[\\]]")[2].split(" ");
        } else {
            res = variables.get(op).split("[\\[\\]]")[2].split(" ");
        }
        if (res[0].equals("")) {
            return new String[0];
        }
        return res;
    }
    private static State handleLiteral(String literal) throws Exception {
        if (isOp(literal)) {
            opStack.add(new Operation(literal, getOperandReq(literal)));
            return State.NORMAL;
        } else if (variables.containsKey(literal) && variables.get(literal).charAt(0) == '[') {
            opStack.add(new Operation(literal, getArgs(literal).length));
            return State.NORMAL;
        } else if (!localStack.isEmpty() && localStack.get(localStack.size() - 1).containsKey(literal) && localStack.get(localStack.size() - 1).get(literal).charAt(0) == '[') {
            opStack.add(new Operation(literal, getArgs(literal).length));
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
            String value;
            if (localStack.isEmpty()) {
                value = variables.get(literal.substring(1));
            } else {
                value = localStack.get(localStack.size() - 1).get(literal.substring(1));
                if (value.equals(null)) value = variables.get(literal.substring(1));
            }
            if (value.equals(null)) throw new Exception("Undefined variable: " + literal.substring(1));
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
        //String op3 = ":";
        String op2 = "*/%";
        String op1 = "+-";
        String op = op0 + op1 + op2 ;
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

            } else if (cur == ':') {
                i = i + 1;
                StringBuilder name = new StringBuilder(String.valueOf(expression.charAt(i)));
                while (i + 1 < expression.length() && Character.isLetterOrDigit(expression.charAt(i + 1) )) {
                    name.append(expression.charAt(i + 1));
                    i++;
                }

                if (!localStack.isEmpty() && localStack.get(localStack.size() - 1).containsKey(name.toString())) {
                    operands.add(localStack.get(localStack.size() - 1).get(name.toString()));
                } else {
                    operands.add(variables.get(name.toString()));
                }
                state = "num";
            } else if (Character.isLetter(cur)) {
                StringBuilder name = new StringBuilder(String.valueOf(cur));
                while (i + 1 < expression.length() && Character.isLetterOrDigit(expression.charAt(i + 1) )) {
                    name.append(expression.charAt(i + 1));
                    i++;
                }
                String literal = name.toString();
                if (binOps.contains(literal) || unaOps.contains(literal)) {
                    operators.add(literal);
                } else if (variables.containsKey(literal)) { // Local or global function
                    operators.add(literal);
                } else {
                    throw new Error ("Invalid Operands");
                }
                priority.put(literal, 3);
                state = "num";
            }
        }
        return operands.get(0);
    }

    private static void cleanStack(ArrayList<String> operands, ArrayList<String> operators) throws Exception {
        while (!operators.isEmpty() && !operators.get(operators.size() - 1).equals("(")) {
            String Op = operators.remove(operators.size() - 1);
            double res;
            switch (Op) {
                case "+":
                case "add":
                    res = Double.parseDouble(operands.remove(operands.size() - 2)) + Double.parseDouble(operands.remove(operands.size() - 1));
                    break;
                case "-":
                    res = Double.parseDouble(operands.remove(operands.size() - 2)) - Double.parseDouble(operands.remove(operands.size() - 1));
                    break;
                case "*":
                    res = Double.parseDouble(operands.remove(operands.size() - 2)) * Double.parseDouble(operands.remove(operands.size() - 1));
                    break;
                case "/":
                    res = Double.parseDouble(operands.remove(operands.size() - 2)) / Double.parseDouble(operands.remove(operands.size() - 1));
                    break;
                case "%":
                    res = Double.parseDouble(operands.remove(operands.size() - 2)) % Double.parseDouble(operands.remove(operands.size() - 1));
                    break;
                case ":":
                    if (!localStack.isEmpty()  && localStack.get(localStack.size() - 1).containsKey(operands.size() - 1)) {
                        res = Double.parseDouble(localStack.get(localStack.size() - 1).get(operands.remove(operands.size() - 1)));
                    } else {
                        res = Double.parseDouble(variables.get(operands.remove(operands.size() - 1)));
                    }
                    break;
                default:
                    if (variables.containsKey(Op)) {
                        res = Double.parseDouble(callSubProgram(Op, operands));
                        break;
                    } else if (binOps.contains(Op)) {
                        res = Double.parseDouble(eval(Op, operands.remove(operands.size() - 2), operands.remove(operands.size() - 1)));
                        break;
                    } else if (unaOps.contains(Op)) {
                        res = Double.parseDouble(eval(Op, operands.remove(operands.size() - 1)));
                        break;
                    }
                    throw new Exception("Invalid Operator");
            }
            operands.add(Double.toString(res));
        }
    }
}
