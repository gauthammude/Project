import java.util.*;

public class CalculatorWithQueue {
    private static boolean isFloatMode;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose mode:\n1. Integer\n2. Float");
        int mode = scanner.nextInt();
        scanner.nextLine();  // consume newline
        isFloatMode = (mode == 2);

        System.out.println("Enter a mathematical expression:");
        String rawInput = scanner.nextLine().replaceAll(" ", "");
        String processedInput = insertImplicitMultiplication(rawInput);

        Queue<String> tokens = tokenize(processedInput);

        try {
            if (isFloatMode) {
                double result = parseExpressionFloat(new LinkedList<>(tokens));
                System.out.println("Result: " + result);
            } else {
                int result = parseExpressionInt(new LinkedList<>(tokens));
                System.out.println("Result: " + result);
            }
        } catch (RuntimeException e) {
            System.out.println("Invalid expression: " + e.getMessage());
        }

        scanner.close();
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

    private static Queue<String> tokenize(String expr) {
        Queue<String> tokens = new LinkedList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                boolean dotSeen = false;
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || (!dotSeen && expr.charAt(i) == '.'))) {
                    if (expr.charAt(i) == '.') dotSeen = true;
                    num.append(expr.charAt(i++));
                }
                tokens.add(num.toString());
            } else {
                tokens.add(String.valueOf(c));
                i++;
            }
        }
        return tokens;
    }

    // ==== Integer parsing with Queue ====

    private static int parseExpressionInt(Queue<String> tokens) {
        int value = parseTermInt(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("+")) {
                tokens.poll();
                value += parseTermInt(tokens);
            } else if (op.equals("-")) {
                tokens.poll();
                value -= parseTermInt(tokens);
            } else {
                break;
            }
        }
        return value;
    }

    private static int parseTermInt(Queue<String> tokens) {
        int value = parseFactorInt(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("*")) {
                tokens.poll();
                value *= parseFactorInt(tokens);
            } else if (op.equals("/")) {
                tokens.poll();
                int divisor = parseFactorInt(tokens);
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                tokens.poll();
                int divisor = parseFactorInt(tokens);
                if (divisor == 0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else {
                break;
            }
        }
        return value;
    }

    private static int parseFactorInt(Queue<String> tokens) {
        if (tokens.isEmpty()) throw new RuntimeException("Unexpected end of expression");
        String token = tokens.poll();

        if (token.equals("-")) {
            return -parseFactorInt(tokens);
        } else if (token.equals("(")) {
            int value = parseExpressionInt(tokens);
            if (tokens.isEmpty() || !tokens.poll().equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return value;
        } else {
            return Integer.parseInt(token);
        }
    }

    // ==== Float parsing with Queue ====

    private static double parseExpressionFloat(Queue<String> tokens) {
        double value = parseTermFloat(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("+")) {
                tokens.poll();
                value += parseTermFloat(tokens);
            } else if (op.equals("-")) {
                tokens.poll();
                value -= parseTermFloat(tokens);
            } else {
                break;
            }
        }
        return value;
    }

    private static double parseTermFloat(Queue<String> tokens) {
        double value = parseFactorFloat(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if (op.equals("*")) {
                tokens.poll();
                value *= parseFactorFloat(tokens);
            } else if (op.equals("/")) {
                tokens.poll();
                double divisor = parseFactorFloat(tokens);
                if (divisor == 0.0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op.equals("%")) {
                tokens.poll();
                double divisor = parseFactorFloat(tokens);
                if (divisor == 0.0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else {
                break;
            }
        }
        return value;
    }

    private static double parseFactorFloat(Queue<String> tokens) {
        if (tokens.isEmpty()) throw new RuntimeException("Unexpected end of expression");
        String token = tokens.poll();

        if (token.equals("-")) {
            return -parseFactorFloat(tokens);
        } else if (token.equals("(")) {
            double value = parseExpressionFloat(tokens);
            if (tokens.isEmpty() || !tokens.poll().equals(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return value;
        } else {
            return Double.parseDouble(token);
        }
    }
}
