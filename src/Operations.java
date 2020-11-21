package src;

import java.util.Map;

import static src.Interpreter.readline;

public class Operations {
    static public String[] Operations1 = {"thing", "print", "not", "erase", "isname", "run", "isnumber", "isbool", "isword", "isempty"};
    static public String[] Operations2 = {"make", "thing", "add", "sub", "mul", "div", "mod", "gt", "eq", "lt", "and", "or"};
    static public String[] Operations3 = {"if"};

    public static String invoke(String operation, Map<String, String> variables, String var1, String var2) throws Exception {
        switch (operation) {
        case "make" :
            return make(variables, var1, var2);
        case "thing" :
            return thing(variables, var1);
        case "print" :
            return print(var1);
        case "add" :
            return add(var1, var2);
        case "sub" :
            return sub(var1, var2);
        case "mul" :
            return mul(var1, var2);
        case "div" :
            return div(var1, var2);
        case "mod" :
            return mod(var1, var2);
        case "eq" :
            return eq(var1, var2);
        case "gt" :
            return gt(var1, var2);
        case "lt" :
            return lt(var1, var2);
        case "and" :
            return and(var1, var2);
        case "or" :
            return or(var1, var2);
        default :
            System.err.println("Invalid Operation");
            return null;
    }
    }

    public static String invoke(String operation, Map<String, String> variables, String var1) throws Exception {
        switch (operation) {
            case "thing" :
                return thing(variables, var1);
            case "print" :
                return print(var1);
            case "erase" :
                return erase(variables, var1);
            case "isname" :
                return isname(variables, var1);
            case "isnumber" :
                return isnumber(var1);
            case "isword" :
                return isword(var1);
            case "isempty" :
                return isempty(var1);
            case "run" :
                return run(var1);
            case "not" :
                return not(var1);
            case "isbool" :
                return isbool(var1);
            default :
                System.err.println("Invalid Operation");
                return null;
        }
    }
    
    static String make(Map<String, String> variables, String name, String value) {
        variables.put(name, value);
        return value;
    }

    static String thing(Map<String, String> variables, String name) {
        return variables.get(name);
    }

    static String print(String name) {
        System.out.println(name);
        return name;
    }

    static String erase(Map<String, String> variables, String name) {
        return variables.remove(name);
    }

    static String isname(Map<String, String> variables, String name) {
        return variables.containsKey(name) ? "true" : "false";
    }

    static String isword(String name) {
        String prefixes = "\"[(";
        return prefixes.indexOf(name.charAt(0)) == -1 ? "true" : "false";
    }

    static String isempty(String name) {
        String prefixes = "\"[(";
        return prefixes.indexOf(name.charAt(0)) == -1 ? "true" : "false";
    }

    static String run(String name) {
        String res = "";
        try {
            res = readline(name.substring(1, name.length() - 1));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return res;
    }

    static String not(String name) throws Exception {
        boolean v = !stb(name);
        return (v) ? "true" : "false";

    }

    static String add(String var1, String var2) {
        float res = Float.parseFloat(var1) + Float.parseFloat(var2);
        return Float.toString(res);
    }

    static String sub(String var1, String var2) {
        float res = Float.parseFloat(var1) - Float.parseFloat(var2);
        return Float.toString(res);
    }

    static String mul(String var1, String var2) {
        float res = Float.parseFloat(var1) * Float.parseFloat(var2);
        return Float.toString(res);
    }

    static String div(String var1, String var2) {
        float res = Float.parseFloat(var1) / Float.parseFloat(var2);
        return Float.toString(res);
    }

    static String mod(String var1, String var2) {
        float res = Float.parseFloat(var1) % Float.parseFloat(var2);
        return Float.toString(res);
    }

    static String eq(String var1, String var2) { 
        return (var1.equals(var2))? "true" : "false";
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    static String isnumber(String name) {
        return isNumeric(name) ? "true" : "false";
    }

    static String isbool(String name) {
        return (name.equals("true") || name.equals("false") ? "true" : "false");
    }

    static String gt(String var1, String var2) {
        if (isNumeric(var1) && isNumeric(var2)) {
            return (Double.parseDouble(var1) > Double.parseDouble(var2)) ? "true" : "false";
        } else {
            return (var1.compareTo(var2) > 0) ? "true" : "false";
        }
    }

    static String lt(String var1, String var2) {
        if (isNumeric(var1) && isNumeric(var2)) {
            return (Double.parseDouble(var1) < Double.parseDouble(var2)) ? "true" : "false";
        } else {
            return (var1.compareTo(var2) < 0) ? "true" : "false";
        }
    }

    static boolean stb(String v) throws Exception {
        if (v.equals("true")) {
            return true;
        } else if (v.equals("false")) {
            return false;
        } else {
            throw new Exception("illegal operands: " + v);
        }
    }

    static String and(String var1, String var2) throws Exception{
        boolean v1, v2;
        v1 = stb(var1);
        v2 = stb(var2);
        return (v1 && v2) ? "true" : "false";
    }

    static String or(String var1, String var2) throws Exception {
        boolean v1, v2;
        v1 = stb(var1);
        v2 = stb(var2);
        return (v1 || v2) ? "true" : "false";
    }

}