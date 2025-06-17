import java.util.ArrayList;
import java.util.Scanner;

public class calaculatorwithArrayList {
    private static ArrayList<String> tokens;
    private static int index;
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
        index = 0;

        try {
            if (isFloatMode) {
                double result = parseExpressionFloat();
                if (index != tokens.size()) {
                    throw new RuntimeException("Unexpected token at position " + index);
                }
                System.out.println("Result: " + result);
            } else {
                int result = parseExpressionInt();
                if (index != tokens.size()) {
                    throw new RuntimeException("Unexpected token at position " + index);
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

    private static ArrayList<String> tokenize(String expr) {
        ArrayList<String> tokens = new ArrayList<>();
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

    // Integer parsing methods: parseExpressionInt(), parseTermInt(), parseFactorInt()
    // Use tokens.get(index) instead of input.charAt(index)
    // Advance index accordingly.

    private static int parseExpressionInt() {
        int value = parseTermInt();
        while (index < tokens.size()) {
            String op = tokens.get(index);
            if (op.equals("+")) {
                index++;
                value += parseTermInt();
            } else if (op.equals("-")) {
                index++;
                value -= parseTermInt();
            } else {
                break;
            }
        }
        return value;
    }

    private static int parseTermInt() {
        int value = parseFactorInt();
        while (index < tokens.size()) {
            String op = tokens.get(index);
            if (op.equals("*")) {
                index++;
                value *= parseFactorInt();
            } else if (op.equals("/")) {
                index++;
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                index++;
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
        if (index >= tokens.size()) throw new RuntimeException("Unexpected end of expression");

        String token = tokens.get(index);

        if (token.equals("-")) {
            index++;
            return -parseFactorInt();
        }

        if (token.equals("(")) {
            index++;
            int value = parseExpressionInt();
            if (index >= tokens.size() || !tokens.get(index).equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            index++;
            return value;
        }

        try {
            int value = Integer.parseInt(token);
            index++;
            return value;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Expected number but found '" + token + "' at position " + index);
        }
    }

    // Float parsing methods (similar changes as Integer parsing)

    private static double parseExpressionFloat() {
        double value = parseTermFloat();
        while (index < tokens.size()) {
            String op = tokens.get(index);
            if (op.equals("+")) {
                index++;
                value += parseTermFloat();
            } else if (op.equals("-")) {
                index++;
                value -= parseTermFloat();
            } else {
                break;
            }
        }
        return value;
    }

    private static double parseTermFloat() {
        double value = parseFactorFloat();
        while (index < tokens.size()) {
            String op = tokens.get(index);
            if (op.equals("*")) {
                index++;
                value *= parseFactorFloat();
            } else if (op.equals("/")) {
                index++;
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                index++;
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
        if (index >= tokens.size()) throw new RuntimeException("Unexpected end of expression");

        String token = tokens.get(index);

        if (token.equals("-")) {
            index++;
            return -parseFactorFloat();
        }

        if (token.equals("(")) {
            index++;
            double value = parseExpressionFloat();
            if (index >= tokens.size() || !tokens.get(index).equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            index++;
            return value;
        }

        try {
            double value = Double.parseDouble(token);
            index++;
            return value;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Expected number but found '" + token + "' at position " + index);
        }
    }
}
