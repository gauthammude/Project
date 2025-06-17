import java.util.Scanner;

public class calculator {
    private static int index;
    private static String input;
    private static boolean isFloatMode;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose mode:\n1. Integer\n2. Float");
        int mode = scanner.nextInt();
        scanner.nextLine();  // consume newline
        isFloatMode = (mode == 2);

        System.out.println("Enter a mathematical expression:");
        String rawInput = scanner.nextLine().replaceAll(" ", "");
        input = insertImplicitMultiplication(rawInput);
        index = 0;

        try {
            if (isFloatMode) {
                double result = parseExpressionFloat();
                if (index != input.length()) {
                    throw new RuntimeException("Unexpected character at position " + index);
                }
                System.out.println("Result: " + result);
            } else {
                int result = parseExpressionInt();
                if (index != input.length()) {
                    throw new RuntimeException("Unexpected character at position " + index);
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

    // ==== Integer parsing ====

    private static int parseExpressionInt() {
        int value = parseTermInt();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '+') {
                index++;
                value += parseTermInt();
            } else if (op == '-') {
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
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '*') {
                index++;
                value *= parseFactorInt();
            } else if (op == '/') {
                index++;
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op == '%') {
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
        if (index >= input.length()) throw new RuntimeException("Unexpected end of expression");
        char ch = input.charAt(index);

        if (ch == '-') {
            index++;
            return -parseFactorInt();
        }

        if (ch == '(') {
            index++;
            int value = parseExpressionInt();
            if (index >= input.length() || input.charAt(index) != ')') {
                throw new RuntimeException("Missing closing parenthesis");
            }
            index++;
            return value;
        }

        int start = index;
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }

        if (start == index) throw new RuntimeException("Expected number at position " + index);
        return Integer.parseInt(input.substring(start, index));
    }

    // ==== Float parsing ====

    private static double parseExpressionFloat() {
        double value = parseTermFloat();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '+') {
                index++;
                value += parseTermFloat();
            } else if (op == '-') {
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
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '*') {
                index++;
                value *= parseFactorFloat();
            } else if (op == '/') {
                index++;
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op == '%') {
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
        if (index >= input.length()) throw new RuntimeException("Unexpected end of expression");
        char ch = input.charAt(index);

        if (ch == '-') {
            index++;
            return -parseFactorFloat();
        }

        if (ch == '(') {
            index++;
            double value = parseExpressionFloat();
            if (index >= input.length() || input.charAt(index) != ')') {
                throw new RuntimeException("Missing closing parenthesis");
            }
            index++;
            return value;
        }

        int start = index;
        boolean dotSeen = false;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isDigit(c)) {
                index++;
            } else if (c == '.' && !dotSeen) {
                dotSeen = true;
                index++;
            } else {
                break;
            }
        }

        if (start == index) throw new RuntimeException("Expected number at position " + index);
        return Double.parseDouble(input.substring(start, index));
    }
}
