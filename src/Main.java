package src;

public class Main {
    public static void main(String[] args) {
        //System.out.println("Welcome to mua");
        try {
            Interpreter.mainLoop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}