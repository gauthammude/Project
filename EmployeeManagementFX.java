import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmployeeManagementFX extends Application {

    // --- Database Credentials ---
    private final String URL = "jdbc:mysql://localhost:3306/student";
    private final String USER = "root";
    private final String PASSWORD = "gautham";

    // --- UI Components ---
    private TableView<Employee> table;
    private TextField idField, nameField, emailField, salaryField, searchField;
    private Button addBtn, updateBtn, deleteBtn, clearBtn;
    private Label statusLabel;

    // --- Data Collections ---
    private ObservableList<Employee> employeeData = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredData;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Employee Management System (User-Generated ID)");

        createTableIfNotExists();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Search Bar
        searchField = new TextField();
        searchField.setPromptText("Search by Name or Email...");
        HBox searchBox = new HBox(new Label("Search: "), searchField);
        searchBox.setPadding(new Insets(0, 0, 10, 0));
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        root.setTop(searchBox);

        // Center: TableView
        setupTable();
        root.setCenter(table);

        // Right: Input Form and Buttons
        GridPane formPane = createFormPane();
        root.setRight(formPane);

        // Bottom: Status Bar
        statusLabel = new Label("Ready.");
        statusLabel.setPadding(new Insets(5, 0, 0, 0));
        root.setBottom(statusLabel);

        setupEventHandlers();
        setupBindings(); // MODIFIED: Bindings are now based on field content
        setupFiltering();

        loadEmployees();

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

        // MODIFIED: ID field is now editable
        idField = new TextField();
        idField.setPromptText("Employee ID");
        // idField.setEditable(false); // REMOVED

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
        clearBtn.setOnAction(e -> clearFields());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearFields();
            }
        });
    }

    // MODIFIED: Button bindings are now based on text fields being empty
    private void setupBindings() {
        BooleanBinding idFieldEmpty = idField.textProperty().isEmpty();
        BooleanBinding nameFieldEmpty = nameField.textProperty().isEmpty();
        BooleanBinding salaryFieldEmpty = salaryField.textProperty().isEmpty();

        // Add button is disabled if ID, Name, or Salary is empty
        addBtn.disableProperty().bind(idFieldEmpty.or(nameFieldEmpty).or(salaryFieldEmpty));

        // Update and Delete buttons are disabled if the ID field is empty
        updateBtn.disableProperty().bind(idFieldEmpty);
        deleteBtn.disableProperty().bind(idFieldEmpty);
    }

    private void setupFiltering() {
        // (Filtering logic is unchanged)
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

    // --- Database and Logic Methods ---

    private void loadEmployees() {
        // (Unchanged)
        employeeData.clear();
        String sql = "SELECT * FROM employees ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employeeData.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getDouble("salary")
                ));
            }
            statusLabel.setText("Loaded " + employeeData.size() + " employees.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load employees from the database.");
        }
    }

    // MODIFIED: handleAdd now takes the ID from the user
    private void handleAdd() {
        if (!validateInput(true)) return; // Pass true to indicate it's an "add" operation

        int id = Integer.parseInt(idField.getText().trim());
        String email = emailField.getText().trim();

        // NEW: Check if ID already exists before trying to insert
        if (checkIdExists(id)) {
            showAlert(Alert.AlertType.ERROR, "ID Exists", "An employee with ID " + id + " already exists. Please use a different ID.");
            return;
        }

        String sql = "INSERT INTO employees (id, name, email, salary) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Set the user-provided ID
            pstmt.setString(2, nameField.getText().trim());
            pstmt.setString(3, email.isEmpty() ? null : email);
            pstmt.setDouble(4, Double.parseDouble(salaryField.getText().trim()));
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully.");
            loadEmployees();
            clearFields();
        } catch (SQLException e) {
            // This can still happen in a multi-user environment (race condition)
            if (e.getSQLState().equals("23000")) { // Integrity constraint violation
                 showAlert(Alert.AlertType.ERROR, "ID Exists", "An employee with ID " + id + " already exists.");
            } else {
                 e.printStackTrace();
                 showAlert(Alert.AlertType.ERROR, "Add Error", "Failed to add employee.");
            }
        }
    }

    // MODIFIED: handleDelete now gets the ID directly from the field
    private void handleDelete() {
        // Validate that the ID field contains a valid number
        if (idField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter an ID to delete.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "The ID must be a valid integer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Employee with ID: " + id);
        confirmAlert.setContentText("Are you sure? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM employees WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                     showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted successfully.");
                     loadEmployees();
                     clearFields();
                } else {
                     showAlert(Alert.AlertType.WARNING, "Not Found", "No employee with ID " + id + " was found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete employee.");
            }
        }
    }

    // handleUpdate remains logically similar, but validation is now stricter
    private void handleUpdate() {
        if (!validateInput(false)) return; // Pass false for "update" validation

        String email = emailField.getText().trim();
        int id = Integer.parseInt(idField.getText().trim());

        String sql = "UPDATE employees SET name = ?, email = ?, salary = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText().trim());
            pstmt.setString(2, email.isEmpty() ? null : email);
            pstmt.setDouble(3, Double.parseDouble(salaryField.getText().trim()));
            pstmt.setInt(4, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully.");
                loadEmployees();
                clearFields();
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found", "No employee with ID " + id + " was found to update.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update employee.");
        }
    }

    // --- Utility Methods ---

    // MODIFIED: Validation now checks the ID field
    private boolean validateInput(boolean isAdding) {
        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String salary = salaryField.getText().trim();
        String errorMessage = "";

        if (idText.isEmpty()) errorMessage += "ID field cannot be empty.\n";
        else {
            try {
                Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                errorMessage += "ID must be a valid integer.\n";
            }
        }
        if (name.isEmpty()) errorMessage += "Name field cannot be empty.\n";
        if (salary.isEmpty()) errorMessage += "Salary field cannot be empty.\n";
        else {
            try {
                Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                errorMessage += "Salary must be a valid number.\n";
            }
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage += "Please enter a valid email address.\n";
        }
        
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage);
            return false;
        }
    }

    // NEW: Helper method to check if an ID exists in the database
    private boolean checkIdExists(int id) {
        String sql = "SELECT COUNT(*) FROM employees WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void populateFields(Employee employee) {
        // (Unchanged)
        idField.setText(String.valueOf(employee.getId()));
        nameField.setText(employee.getName());
        emailField.setText(employee.getEmail() == null ? "" : employee.getEmail());
        salaryField.setText(String.valueOf(employee.getSalary()));
    }

    private void clearFields() {
        // (Unchanged)
        idField.clear();
        nameField.clear();
        emailField.clear();
        salaryField.clear();
        table.getSelectionModel().clearSelection();
        nameField.requestFocus();
        statusLabel.setText("Ready.");
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        // (Unchanged)
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // MODIFIED: createTable now defines ID as a non-auto-incrementing PRIMARY KEY
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                "id INT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE, " +
                "salary DECIMAL(10, 2) NOT NULL)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Fatal Database Error", "Could not create the 'employees' table. The application will now exit.");
            System.exit(1);
        }
    }

    public static class Employee {
        // (Unchanged)
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