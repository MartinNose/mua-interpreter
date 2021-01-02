package mua;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static mua.Interpreter.*;

public class Operations {
    static public String[] Operations0 = {"erall", "poall", "read", "readlist"};
    static public String[] Operations1 = {"thing", "print", "not", "erase", "isname", "run", "isnumber", "isbool", "isword", "isempty", "islist", "return", "export", "first", "last", "butfirst", "butlast", "random", "int", "sqrt", "save", "load"};
    static public String[] Operations2 = {"make", "thing", "add", "sub", "mul", "div", "mod", "gt", "eq", "lt", "and", "or", "word", "sentence", "list", "join"};
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
            case "word" :
                return word(var1, var2);
            case "sentence" :
                return sentence(var1, var2);
            case "list":
                return list(var1, var2);
            case "join":
                return join(var1, var2);
            default :
                System.err.println("Invalid Operation");
                return null;
        }
    }

    public static String invoke(String operation) throws Exception {
        switch(operation) {
            case "erall":
                return erall();
            case "poall" :
                return poall();
            default:
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
            case "islist" :
                return islist(var1);
            case "run" :
                return run(var1);
            case "not" :
                return not(var1);
            case "isbool" :
                return isbool(var1);
            case "return" :
                return returnvalue(var1);
            case "export" :
                return export(variables, var1);
            case "first" :
                return first(var1);
            case "last" :
                return last(var1);
            case "butfirst" :
                return butfirst(var1);
            case "butlast" :
                return butlast(var1);
            case "int" :
                return floor(var1);
            case "random" :
                return random(var1);
            case "sqrt" :
                return sqrt(var1);
            case "save" :
                return save(var1);
            case "load" :
                return load(var1);
            default :
                System.err.println("Invalid Operation");
                return null;
        }
    }

    public static String invoke(String operation, Map<String, String> variables, Map<String, String> localV) throws Exception {
        if (!variables.containsKey(operation)) throw new Exception("Function not found");
        return "1";
    }

    static String make(Map<String, String> variables, String name, String value) {
        if (localStack.isEmpty()) {
            variables.put(name, value);
            return value;
        } else {
            localStack.get(localStack.size() - 1).put(name, value);
            return value;
        }


    }

    static String thing(Map<String, String> variables, String name) {
        return variables.get(name);
    }

    static String export(Map<String, String> variables, String name) {
        if (localStack.isEmpty()) return null;
        String res = localStack.get(localStack.size() - 1).get(name);
        variables.put(name, res);
        return res;
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
        if (isNumeric(name)) return "false";
        return prefixes.indexOf(name.charAt(0)) == -1 ? "true" : "false";
    }

    static String isempty(String name) {
        return  (name.matches("\\[\\s*\\]") || name.equals("")) ? "true" : "false";
    }

    static String islist(String name) {
        return  (name.matches("\\[.*\\]") || name.equals("")) ? "true" : "false";
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

    static ArrayList<String> getList(String list) {
        ArrayList<String> res = new ArrayList<>();
        list = list.substring(1, list.length() - 1);
        int bCnt = 0;
        StringBuilder item = new StringBuilder();
        for (int i = 0; i < list.length(); i++) {
            if (list.charAt(i) == '[') {
                bCnt++;
                item.append(list.charAt(i));
            } else if (list.charAt(i) == ']') {
                bCnt--;
                item.append(list.charAt(i));
            } else {
                if (bCnt != 0) {
                    item.append(list.charAt(i));
                } else {
                    if (list.charAt(i) == ' ') {
                        res.add(item.toString());
                        item.setLength(0);
                    } else {
                        item.append(list.charAt(i));
                    }
                }
            }
        }
        if (item.length() != 0) {
            res.add(item.toString());
        }
        return res;
    }

    static String listToString(ArrayList<String> list) {
        StringBuilder newlist = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            newlist.append(list.get(i));
            newlist.append(' ');
        }
        if(newlist.length() == 0) return "[]";
        newlist.setLength(newlist.length() - 1);
        return "[" + newlist.toString() + "]";
    }

    static String first(String name) {
        if (name.charAt(0) == '[') {
            return getList(name).get(0);
        } else {
            return Character.toString(name.charAt(0));
        }
    }

    static String last(String name) {
        if (name.charAt(0) == '[') {
            ArrayList<String> res = getList(name);
            return res.get(res.size() - 1);
        } else {
            return Character.toString(name.charAt(name.length() - 1));
        }
    }

    static String butfirst(String name) {
        if (name.charAt(0) == '[') {
            ArrayList<String> list = getList(name);
            list.remove(0);
            return listToString(list);
        } else {
            return name.substring(1);
        }
    }

    static String butlast(String name) {
        if (name.charAt(0) == '[') {
            ArrayList<String> list = getList(name);
            list.remove(list.size() - 1);
            return listToString(list);
        } else {
            return name.substring(0, name.length() - 1);
        }
    }

    static String not(String name) throws Exception {
        boolean v = !stb(name);
        return (v) ? "true" : "false";
    }

    static String returnvalue(String var1) {
        return var1;
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
        if (isNumeric(var1) && isNumeric(var2)) {
            return (Double.parseDouble(var1) == Double.parseDouble(var2)) ? "true" : "false";
        }
        return (var1.equals(var2))? "true" : "false";
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    static String random(String var) {
        Random rand = new Random();
        return Integer.toString(rand.nextInt(Integer.parseInt(var)));
    }

    static String floor(String var) {
        return Integer.toString((int)Math.floor(Double.parseDouble(var)));
    }

    static String sqrt(String var) {
        return Double.toString(Math.sqrt(Double.parseDouble(var)));
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

    static String word(String var1, String var2) throws Exception {
        return var1 + var2; // TODO error detection
    }

    static String sentence(String var1, String var2) throws Exception {
        return var1.substring(0, var1.length() - 1) + " " + var2.substring(1);
    }

    static String list(String var1, String var2) throws Exception {
        return "[" + var1 + " " + var2 + "]";
    }

    static String join(String var1, String var2) throws Exception {
        return var1.substring(0, var1.length() - 1) + " " + var2 + "]";
    }

    static Map<String, String> getNameSpace() {
        if (localStack.isEmpty()) {
            return variables;
        } else {
            return localStack.get(localStack.size() - 1);
        }
    }

    static String save(String filename) {
        Map<String, String> namespace = getNameSpace();
        try (PrintWriter out = new PrintWriter(filename)) {
            for (Map.Entry<String, String> entry : namespace.entrySet()) {
                out.println("make \"" + entry.getKey() + " " + entry.getValue());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return filename;
    }

    static String load(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                readline(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return "false";
        }

        return "true";
    }

    static String erall() {
        getNameSpace().clear();
        return "true";
    }

    static String poall() {
        ArrayList<String> names = new ArrayList<>();
        Map<String, String> namespace = getNameSpace();

        for (Map.Entry<String, String> entry : namespace.entrySet()) {
            names.add(entry.getKey());
        }

        return listToString(names);
    }
}