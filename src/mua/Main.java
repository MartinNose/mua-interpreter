package mua;

public class Main {
    public static void main(String[] args) {
        //System.out.println("Welcome to mua");
        try {
            Interpreter.mainLoop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}