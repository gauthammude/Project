import java.util.LinkedList;
import java.util.Scanner;

public class calculatorwithLL {
    private static LinkedList<String> tokens;
    private static boolean isFloatMode;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose mode:\n1. Integer\n2. Float");
        int mode = scanner.nextInt();
        scanner.nextLine();  // consume newline
        isFloatMode = (mode == 2);

        System.out.println("Enter a mathematical expression:");
        String rawInput = scanner.nextLine().replaceAll(" ", "");
        rawInput = insertImplicitMultiplication(rawInput);

        tokens = tokenize(rawInput);

        try {
            if (isFloatMode) {
                double result = parseExpressionFloat();
                if (!tokens.isEmpty()) {
                    throw new RuntimeException("Unexpected token: " + tokens.peek());
                }
                System.out.println("Result: " + result);
            } else {
                int result = parseExpressionInt();
                if (!tokens.isEmpty()) {
                    throw new RuntimeException("Unexpected token: " + tokens.peek());
                }
                System.out.println("Result: " + result);
            }
        } catch (RuntimeException e) {
            System.out.println("Invalid expression: " + e.getMessage());
        }
       
    }
    

    private static String insertImplicitMultiplication(String expr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char current = expr.charAt(i);
            sb.append(current);
            if (i < expr.length() - 1) {
                char next = expr.charAt(i + 1);
                if ((Character.isDigit(current) || current == ')') && next == '(') {
                    sb.append('*');
                }
            }
        }
        return sb.toString();
        
    }

    private static LinkedList<String> tokenize(String expr) {
        LinkedList<String> tokens = new LinkedList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                boolean dotSeen = false;
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || (!dotSeen && expr.charAt(i) == '.'))) {
                    if (expr.charAt(i) == '.') dotSeen = true;
                    number.append(expr.charAt(i));
                    i++;
                }
                tokens.add(number.toString());
            } else {
                tokens.add(String.valueOf(c));
                i++;
            }
        }
        return tokens;
    }

    // === Integer parsing ===

    private static int parseExpressionInt() {
        int value = parseTermInt();
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("+")) {
                tokens.poll();
                value += parseTermInt();
            } else if (op.equals("-")) {
                tokens.poll();
                value -= parseTermInt();
            } else {
                break;
            }
        }
        return value;
    }

    private static int parseTermInt() {
        int value = parseFactorInt();
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("*")) {
                tokens.poll();
                value *= parseFactorInt();
            } else if (op.equals("/")) {
                tokens.poll();
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                tokens.poll();
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else {
                break;
            }
        }
        return value;
    }

    private static int parseFactorInt() {
        if (tokens.isEmpty()) throw new RuntimeException("Unexpected end of expression");

        String token = tokens.poll();

        if (token.equals("-")) {
            return -parseFactorInt();
        }

        if (token.equals("(")) {
            int value = parseExpressionInt();
            if (tokens.isEmpty() || !tokens.poll().equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return value;
        }

        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Expected number but found '" + token + "'");
        }
    }

    //Float

    private static double parseExpressionFloat() {
        double value = parseTermFloat();
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("+")) {
                tokens.poll();
                value += parseTermFloat();
            } else if (op.equals("-")) {
                tokens.poll();
                value -= parseTermFloat();
            } else {
                break;
            }
        }
        return value;
    }

    private static double parseTermFloat() {
        double value = parseFactorFloat();
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("*")) {
                tokens.poll();
                value *= parseFactorFloat();
            } else if (op.equals("/")) {
                tokens.poll();
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                tokens.poll();
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else {
                break;
            }
        }
        return value;
    }

    private static double parseFactorFloat() {
        if (tokens.isEmpty()) throw new RuntimeException("Unexpected end of expression");

        String token = tokens.poll();

        if (token.equals("-")) {
            return -parseFactorFloat();
        }

        if (token.equals("(")) {
            double value = parseExpressionFloat();
            if (tokens.isEmpty() || !tokens.poll().equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return value;
        }

        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Expected number but found '" + token + "'");
            
        }
        
    }
}
