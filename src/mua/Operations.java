package mua;

import java.util.Map;
import java.util.Scanner;

class Operation {
    String name;
    int operandsNum;
}

public class Operations {
    static public String[] Operations1 = {"thing", "print", "read"};
    static public String[] Operations2 = {"make", "thing", "add", "sub", "mul", "div", "mod"};

    public static String invoke(String operation, Map<String, String> variables, String var1, String var2) {
        switch (operation) {
            case "make" :
                return make(variables, var1, var2);
            case "thing" :
                return thing(variables, var1);
            case "print" :
                return print(var1);
            case "read" :
                return read();
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
            default :
                System.err.println("Invalid Operation");
                return null;
        }   
    }

    public static String invoke(String operation, Map<String, String> variables, String var1) {
        switch (operation) {
            case "thing" :
                return thing(variables, var1);
            case "print" :
                return print(var1);
            case "read" :
                return read();
            default :
                System.err.println("Invalid Operation");
                return null;
        }
        
    }

    public static String invoke(String operation) {
        switch (operation) {
            case "read" :
                return read();
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

    static String read() {
        Scanner scanner = new Scanner(System.in);
        while (!scanner.hasNext()) {

        }
        return scanner.nextLine();
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


    
}
