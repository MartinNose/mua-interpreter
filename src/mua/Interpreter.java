package mua;
import java.util.Scanner;

public class Interpreter {
    private String[] BasicOperations = {"make", "thing", "print", "read",
                                        "add", "sub", "mul", "div", "mod",
                                        };

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
        for (String literal : literals) {
            System.out.println(literal);

            switch (state) {
                case NORMAL :
                    switch(literal.charAt(0)) {
                        case '"': 
                            // Handle word
                            break;
                        
                    }
                    break;
                case LISBEG :
                    break;
                default :
                    break;
            }
        }
        return 0;
    }
}
