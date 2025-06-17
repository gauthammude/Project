import java.sql.*;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
public class MainLauncher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== PROGRAM LAUNCHER =====");
            System.out.println("1. Run Calculator");
            System.out.println("2. Run CRUD operations");
            System.out.println("3. Run JavaFx");
            System.out.println("4. Exit");
            System.out.print("Select an application to run: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            System.out.println(); // for spacing

            switch (choice) {
                case 1:
                    Calculator.run();
                    break;
                case 2:
                    EmployeeCRUD.run();
                    break;
                case 3:
                    // RECTIFIED: This is the standard, correct way to launch a JavaFX
                    // application from a separate class. It properly initializes the FX toolkit
                    // and avoids potential lifecycle errors like NoSuchMethodError or IllegalStateException.
                    Application.launch(EmployeeManagementFX.class, args);
                    break;
                case 4:
                    System.out.println("Exiting application. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}

/**
 * A command-line calculator that can handle integer and float expressions.
 * This is a helper class, so it is not public.
 */
class Calculator {
    private static int index;
    private static String input;
    private static boolean isFloatMode;

    public static void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Calculator ---");
        System.out.println("Choose mode:\n1. Integer\n2. Float");
        int mode = scanner.nextInt();
        scanner.nextLine(); // consume newline
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
        System.out.println("Returning to main menu...");
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

    private static int parseExpressionInt() {
        int value = parseTermInt();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '+') { index++; value += parseTermInt(); }
            else if (op == '-') { index++; value -= parseTermInt(); }
            else { break; }
        }
        return value;
    }
    private static int parseTermInt() {
        int value = parseFactorInt();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '*') { index++; value *= parseFactorInt(); }
            else if (op == '/') {
                index++;
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op == '%') {
                index++;
                int divisor = parseFactorInt();
                if (divisor == 0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else { break; }
        }
        return value;
    }
    private static int parseFactorInt() {
        if (index >= input.length()) throw new RuntimeException("Unexpected end of expression");
        char ch = input.charAt(index);
        if (ch == '-') { index++; return -parseFactorInt(); }
        if (ch == '(') {
            index++;
            int value = parseExpressionInt();
            if (index >= input.length() || input.charAt(index) != ')') throw new RuntimeException("Missing closing parenthesis");
            index++;
            return value;
        }
        int start = index;
        while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
        if (start == index) throw new RuntimeException("Expected number at position " + index);
        return Integer.parseInt(input.substring(start, index));
    }
    private static double parseExpressionFloat() {
        double value = parseTermFloat();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '+') { index++; value += parseTermFloat(); }
            else if (op == '-') { index++; value -= parseTermFloat(); }
            else { break; }
        }
        return value;
    }
    private static double parseTermFloat() {
        double value = parseFactorFloat();
        while (index < input.length()) {
            char op = input.charAt(index);
            if (op == '*') { index++; value *= parseFactorFloat(); }
            else if (op == '/') {
                index++;
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Division by zero");
                value /= divisor;
            } else if (op == '%') {
                index++;
                double divisor = parseFactorFloat();
                if (divisor == 0.0) throw new ArithmeticException("Modulo by zero");
                value %= divisor;
            } else { break; }
        }
        return value;
    }
    private static double parseFactorFloat() {
        if (index >= input.length()) throw new RuntimeException("Unexpected end of expression");
        char ch = input.charAt(index);
        if (ch == '-') { index++; return -parseFactorFloat(); }
        if (ch == '(') {
            index++;
            double value = parseExpressionFloat();
            if (index >= input.length() || input.charAt(index) != ')') throw new RuntimeException("Missing closing parenthesis");
            index++;
            return value;
        }
        int start = index;
        boolean dotSeen = false;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isDigit(c)) { index++; }
            else if (c == '.' && !dotSeen) { dotSeen = true; index++; }
            else { break; }
        }
        if (start == index) throw new RuntimeException("Expected number at position " + index);
        return Double.parseDouble(input.substring(start, index));
    }
}

/**
 * A CRUD application for managing employee records in a MySQL database.
 * This is a helper class, so it is not public.
 */
class EmployeeCRUD {
    private static final String URL = "jdbc:mysql://localhost:3306/student";
    private static final String USER = "root";
    private static final String PASSWORD = "gautham";
    public static void run() {
        Scanner scanner = new Scanner(System.in);
        if (!createTable()) {
             System.out.println("Could not connect to the database. Returning to main menu.");
             return;
        }
        while (true) {
            System.out.println("\n=== Employee Management System ===");
            System.out.println("1. Create Employee");
            System.out.println("2. Read All Employees");
            System.out.println("3. Update Employee Salary");
            System.out.println("4. Delete Employee");
            System.out.println("5. Return to Main Menu");
            System.out.print("Select an option: ");
            int choice = -1;
             try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            switch (choice) {
                case 1 -> {
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter salary: ");
                    double salary = Double.parseDouble(scanner.nextLine());
                    insertEmployee(name, email, salary);
                }
                case 2 -> readEmployees();
                case 3 -> {
                    System.out.print("Enter employee ID to update: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter new salary: ");
                    double newSalary = Double.parseDouble(scanner.nextLine());
                    updateEmployeeSalary(id, newSalary);
                }
                case 4 -> {
                    System.out.print("Enter employee ID to delete: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    deleteEmployee(id);
                }
                case 5 -> {
                    System.out.println("Returning to main menu...");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
    private static boolean createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS employees ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "email VARCHAR(100), "
                + "salary DECIMAL(10, 2))";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection error. Check configuration and ensure MySQL server is running.");
            return false;
        }
    }
    private static void insertEmployee(String name, String email, double salary) {
        String sql = "INSERT INTO employees (name, email, salary) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setDouble(3, salary);
            pstmt.executeUpdate();
            System.out.println("Employee added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void readEmployees() {
        String sql = "SELECT * FROM employees";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- Employees ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Email: %s | Salary: %.2f%n",
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getDouble("salary"));
            }
            if (!found) { System.out.println("No employees found."); }
            System.out.println("-----------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void updateEmployeeSalary(int id, double newSalary) {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newSalary);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("Salary updated.");
            else System.out.println("Employee not found.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("Employee deleted.");
            else System.out.println("Employee not found.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


/**
 * RECTIFIED: This class now uses the standard JavaFX launch mechanism and
 * relies on the `start` method as its entry point. The non-standard `run` method
 * has been removed to improve correctness and maintainability.
 */
class EmployeeManagementFX extends Application {

    private final String URL = "jdbc:mysql://localhost:3306/student";
    private final String USER = "root";
    private final String PASSWORD = "gautham";

    private TableView<Employee> table;
    private TextField idField, nameField, emailField, salaryField, searchField;
    private Button addBtn, updateBtn, deleteBtn, clearBtn;
    private Label statusLabel;
    private GridPane formPane;

    private final ObservableList<Employee> employeeData = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredData;

    private ExecutorService executor;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE
    );

    // This main method is standard for allowing the FX app to be run in isolation.
    public static void main(String[] args) {
        launch(args);
    }
    
    // The non-standard run() method has been removed.

    @Override
    public void init() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Employee Management System (User-Generated ID)");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        searchField = new TextField();
        searchField.setPromptText("Search by Name or Email...");
        HBox searchBox = new HBox(new Label("Search: "), searchField);
        searchBox.setPadding(new Insets(0, 0, 10, 0));
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        root.setTop(searchBox);
        setupTable();
        root.setCenter(table);
        formPane = createFormPane();
        root.setRight(formPane);
        statusLabel = new Label("Initializing...");
        statusLabel.setPadding(new Insets(5, 0, 0, 0));
        root.setBottom(statusLabel);
        setupEventHandlers();
        setupBindings();
        setupFiltering();
        createTableAndLoadEmployees();
        Scene scene = new Scene(root, 950, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMaxWidth(1f * Integer.MAX_VALUE * 35);
        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setMaxWidth(1f * Integer.MAX_VALUE * 35);
        TableColumn<Employee, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        salaryCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        salaryCol.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        table.getColumns().addAll(idCol, nameCol, emailCol, salaryCol);
    }

    private GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 0, 10));
        idField = new TextField();
        idField.setPromptText("Employee ID");
        nameField = new TextField();
        nameField.setPromptText("Full Name");
        emailField = new TextField();
        emailField.setPromptText("Email Address");
        salaryField = new TextField();
        salaryField.setPromptText("Annual Salary");
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Salary:"), 0, 3);
        grid.add(salaryField, 1, 3);
        addBtn = new Button("Add");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn = new Button("Update");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn = new Button("Delete");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn = new Button("Clear");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        VBox buttonBox = new VBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
        grid.add(buttonBox, 0, 4, 2, 1);
        return grid;
    }

    private void setupEventHandlers() {
        addBtn.setOnAction(e -> handleAdd());
        updateBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleDelete());
        clearBtn.setOnAction(e -> {
            clearFields();
            table.getSelectionModel().clearSelection();
        });
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) { populateFields(newSelection); }
        });
    }

    private void setupBindings() {
        BooleanBinding idFieldEmpty = idField.textProperty().isEmpty();
        BooleanBinding nameFieldEmpty = nameField.textProperty().isEmpty();
        BooleanBinding salaryFieldEmpty = salaryField.textProperty().isEmpty();
        addBtn.disableProperty().bind(idFieldEmpty.or(nameFieldEmpty).or(salaryFieldEmpty));
        updateBtn.disableProperty().bind(idFieldEmpty);
        deleteBtn.disableProperty().bind(idFieldEmpty);
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(employeeData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> filteredData.setPredicate(emp -> {
            if (val == null || val.isEmpty()) return true;
            String lowerCaseFilter = val.toLowerCase();
            if (emp.getName().toLowerCase().contains(lowerCaseFilter)) return true;
            return emp.getEmail() != null && emp.getEmail().toLowerCase().contains(lowerCaseFilter);
        }));
        SortedList<Employee> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
    }

    private void runTask(Task<?> task) {
        setFormDisabled(true);
        statusLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> {
            setFormDisabled(false);
            statusLabel.textProperty().unbind();
        });
        task.setOnFailed(e -> {
            setFormDisabled(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Operation Failed. See log for details.");
            task.getException().printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "The operation failed: " + task.getException().getMessage());
        });
        executor.submit(task);
    }

    private void createTableAndLoadEmployees() {
        Task<ObservableList<Employee>> task = new Task<>() {
            @Override
            protected ObservableList<Employee> call() throws Exception {
                updateMessage("Checking database table...");
                createTableIfNotExists();
                updateMessage("Loading employees...");
                return loadEmployeesFromDB();
            }
        };
        task.setOnSucceeded(e -> {
            employeeData.setAll(task.getValue());
            statusLabel.setText("Loaded " + employeeData.size() + " employees. Ready.");
        });
        runTask(task);
    }

    private void handleAdd() {
        if (!validateInput()) return;
        final int id = Integer.parseInt(idField.getText().trim());
        final String name = nameField.getText().trim();
        final String email = emailField.getText().trim();
        final double salary = Double.parseDouble(salaryField.getText().trim());
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Checking for existing ID...");
                if (checkIdExists(id)) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "ID Exists", "An employee with ID " + id + " already exists."));
                    return false;
                }
                updateMessage("Adding employee...");
                String sql = "INSERT INTO employees (id, name, email, salary) VALUES (?, ?, ?, ?)";
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, name);
                    pstmt.setString(3, email.isEmpty() ? null : email);
                    pstmt.setDouble(4, salary);
                    pstmt.executeUpdate();
                    return true;
                }
            }
        };
        task.setOnSucceeded(e -> {
            if (Boolean.TRUE.equals(task.getValue())) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully.");
                    clearFields();
                    createTableAndLoadEmployees();
                });
            }
        });
        runTask(task);
    }

    private void handleDelete() {
        final int id = Integer.parseInt(idField.getText().trim());
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete employee with ID " + id + "?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Deletion");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            Task<Integer> task = new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    updateMessage("Deleting employee ID: " + id);
                    String sql = "DELETE FROM employees WHERE id = ?";
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, id);
                        return pstmt.executeUpdate();
                    }
                }
            };
            task.setOnSucceeded(e -> {
                int rowsAffected = task.getValue();
                Platform.runLater(() -> {
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted.");
                        clearFields();
                        createTableAndLoadEmployees();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Not Found", "No employee with ID " + id + " was found.");
                    }
                });
            });
            runTask(task);
        }
    }

    private void handleUpdate() {
        if (!validateInput()) return;
        final int id = Integer.parseInt(idField.getText().trim());
        final String name = nameField.getText().trim();
        final String email = emailField.getText().trim();
        final double salary = Double.parseDouble(salaryField.getText().trim());
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                updateMessage("Updating employee ID: " + id);
                String sql = "UPDATE employees SET name = ?, email = ?, salary = ? WHERE id = ?";
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email.isEmpty() ? null : email);
                    pstmt.setDouble(3, salary);
                    pstmt.setInt(4, id);
                    return pstmt.executeUpdate();
                }
            }
        };
        task.setOnSucceeded(e -> {
            int rowsAffected = task.getValue();
            Platform.runLater(() -> {
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated.");
                    clearFields();
                    createTableAndLoadEmployees();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "No employee with ID " + id + " was found to update.");
                }
            });
        });
        runTask(task);
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                "id INT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE, " +
                "salary DECIMAL(10, 2) NOT NULL)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private ObservableList<Employee> loadEmployeesFromDB() throws SQLException {
        ObservableList<Employee> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM employees ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getDouble("salary")
                ));
            }
        }
        return list;
    }

    private boolean checkIdExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (idField.getText().trim().isEmpty()) errorMessage += "ID field cannot be empty.\n";
        else try { Integer.parseInt(idField.getText().trim()); } catch (NumberFormatException e) { errorMessage += "ID must be a valid integer.\n"; }
        if (nameField.getText().trim().isEmpty()) errorMessage += "Name field cannot be empty.\n";
        if (salaryField.getText().trim().isEmpty()) errorMessage += "Salary field cannot be empty.\n";
        else try { Double.parseDouble(salaryField.getText().trim()); } catch (NumberFormatException e) { errorMessage += "Salary must be a valid number.\n"; }
        if (!emailField.getText().trim().isEmpty() && !EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            errorMessage += "Please enter a valid email address.\n";
        }
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage);
            return false;
        }
    }

    private void setFormDisabled(boolean disabled) {
        formPane.setDisable(disabled);
        searchField.setDisable(disabled);
        table.setDisable(disabled);
    }

    private void populateFields(Employee employee) {
        idField.setText(String.valueOf(employee.getId()));
        nameField.setText(employee.getName());
        emailField.setText(employee.getEmail() == null ? "" : employee.getEmail());
        salaryField.setText(String.valueOf(employee.getSalary()));
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        salaryField.clear();
        nameField.requestFocus();
        statusLabel.setText("Ready.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Employee {
        private final int id;
        private final String name;
        private final String email;
        private final double salary;
        public Employee(int id, String name, String email, double salary) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.salary = salary;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public double getSalary() { return salary; }
    }
}