import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SimpleCRUD extends Application {

    private ObservableList<String> data;

    @Override
    public void start(Stage primaryStage) {
        data = FXCollections.observableArrayList();

        ListView<String> listView = new ListView<>(data);

        TextField inputField = new TextField();
        inputField.setPromptText("Enter new item");

        Button addButton = new Button("Add");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");

        // Add new item
        addButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                data.add(text);
                inputField.clear();
            }
        });

        // Update selected item
        updateButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            String text = inputField.getText().trim();
            if (selectedIndex >= 0 && !text.isEmpty()) {
                data.set(selectedIndex, text);
                inputField.clear();
            }
        });

        // Delete selected item
        deleteButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                data.remove(selectedIndex);
                inputField.clear();
            }
        });

        // When selecting an item, show it in inputField for editing
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                inputField.setText(newVal);
            }
        });

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton);
        VBox layout = new VBox(10, listView, inputField, buttons);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 300, 400);

        primaryStage.setTitle("Simple JavaFX CRUD");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
