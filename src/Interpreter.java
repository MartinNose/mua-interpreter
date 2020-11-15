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
    static Map<String, String> variables;
    static ArrayList<Operation> opStack;
    static ArrayList<String> valStack;
    static String buffer;
    static int balanceCnt;
    static int operandCnt;
    static Scanner scanner;

    public static void mainLoop() throws Exception {
        scanner = new Scanner(System.in);
        unaOps = new ArrayList<>(Arrays.asList(Operations.Operations1));
        binOps = new ArrayList<>(Arrays.asList(Operations.Operations2));
        variables = new HashMap<>();


        State state = State.NORMAL;
        opStack = new ArrayList<>();
        valStack = new ArrayList<>();
        operandCnt = 0;

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            String[] literals = line.split("\\s+");
            for (String literal : literals) {
                switch (state) {
                    case NORMAL:
                        state = handleLiteral(literal);
                        while (!opStack.isEmpty()
                                && opStack.get(opStack.size() - 1).operandCnt == 0) {
                            pushVStack(popStack());
                        }
                        break;
                    case LISBEG:
                        // READ the list and store as "[a,b,[c,d],e]"
                        updateBCnt(literal, '[');
                        if (balanceCnt == 0) {
                            buffer += literal;
                            pushVStack(buffer);
                            state = State.NORMAL;
                        }
                        break;
                    case INFIXBEG:
                        if (literal.charAt(0) == '(') {
                            int tmp = balanceCnt;
                            balanceCnt = 0;
                            updateBCnt(literal, '(');
                            if (balanceCnt == 0) {
                                buffer += eval(literal);
                                balanceCnt = tmp;
                                break;
                            }
                            balanceCnt = tmp;

                            if (binOps.contains(literal.substring(1)) || unaOps.contains(literal.substring(1))) {
                                opStack.add(new Operation(literal.substring(1)));
                                break;
                            }
                        }

                        updateBCnt(literal, '(');
                        if (balanceCnt == 0) {
                            buffer += literal;
                            pushVStack(eval(buffer));
                            state = State.NORMAL;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        scanner.close();
    }

    enum State {
        NORMAL,
        LISBEG,
        INFIXBEG
    }

    static void pushVStack(String value) {
        if (!opStack.isEmpty()) {
            valStack.add(value);
            opStack.get(opStack.size() - 1).operandCnt--;
        }
    }

    static private final Pattern numeric = Pattern.compile("-?\\d+(\\.\\d+)?");

    private static int getOperandReq(String op) throws Exception {
        if (op.equals("read")) return 0;
        else if (unaOps.contains(op)) return 1;
        else if (binOps.contains(op)) return 2;
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
        if (unaOps.contains(literal) || binOps.contains(literal)) {
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
            if (unaOps.contains(literal.substring(1)) || binOps.contains(literal.substring(1))) {
                opStack.add(new Operation(literal.substring(1)));
                buffer = "(";
            } else {
                buffer = literal;
            }

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
        // READ the infix expression and process
        // read operands in stack
        // push operator to stack
        // if the operator is of less priority than opstack.last() then
        // eval the current stack and push the result into the stack and push the new operator in
        // if there is a left parenthesis then push it to the stack and continue til there is a right parenthesis
        // if there is a right parenthsis and no left parenthesis in the stack then push the result into valStack
        // if there is a prefix op then its of highest priority
        ArrayList<String> operands = new ArrayList<>();
        ArrayList<Character> operator = new ArrayList<>();
        String op0 = "()";
        String op3 = ":";
        String op2 = "*/%";
        String op1 = "+-";
        String op = op0 + op1 + op2 + op3;
        Map<Character, Integer> priority = new HashMap<>();
        priority.put('+', 1);
        priority.put('-', 1);
        priority.put('*', 2);
        priority.put('/', 2);
        priority.put('%', 2);
        priority.put(':', 3);
        priority.put('(', 0);
        priority.put(')', 0);

        String state = "init";
        double operand;
        for (int i = 0; i < expression.length(); i++) {
            char cur = expression.charAt(i);
            if (cur == '-' && (!state.equals("op") || (i > 0 && expression.charAt(i - 1) == '(')) || cur >= '0' && cur <= '9') {
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
            } else if (op.contains(String.valueOf(cur))) {
                if (cur == '(' || operator.isEmpty() || priority.get(cur) >= priority.get(operator.get(operator.size() - 1))) {
                    operator.add(cur);
                } else {
                    cleanStack(operands, operator);
                }
                state = "op";
            } else if (Character.isLetter(cur)) {
                StringBuilder name = new StringBuilder(String.valueOf(cur));
                while (i + 1 < expression.length() && Character.isLetterOrDigit(expression.charAt(i + 1) )) {
                    name.append(expression.charAt(i + 1));
                    i++;
                }
                operands.add(name.toString());
            }
        }
        return operands.get(0);
    }

    private static void cleanStack(ArrayList<String> operands, ArrayList<Character> operators) throws Exception {
        while (!operators.isEmpty() && operators.get(operators.size() - 1) != '(') {
            char Op = operators.remove(operators.size() - 1);
            double res = switch (Op) {
                case '+' -> Double.parseDouble(operands.remove(operands.size() - 1)) + Double.parseDouble(operands.remove(operands.size() - 1));
                case '-' -> Double.parseDouble(operands.remove(operands.size() - 1)) - Double.parseDouble(operands.remove(operands.size() - 1));
                case '*' -> Double.parseDouble(operands.remove(operands.size() - 1)) * Double.parseDouble(operands.remove(operands.size() - 1));
                case '/' -> Double.parseDouble(operands.remove(operands.size() - 1)) / Double.parseDouble(operands.remove(operands.size() - 1));
                case '%' -> Double.parseDouble(operands.remove(operands.size() - 1)) % Double.parseDouble(operands.remove(operands.size() - 1));
                case ':' -> Double.parseDouble(variables.get(operands.remove(operands.size() - 1)));
                default -> throw new Exception("Invalid Operator");
            };
            operands.add(Double.toString(res));
        }
    }
}
