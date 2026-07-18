package com.example.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class KVStoreGUI extends Application {

    private TextArea responseArea;
    private ComboBox<String> nodeSelector;
    private TextField keyField;
    private TextField valueField;
    private Label statusLabel;

    // Node ports
    private final List<String> nodes = Arrays.asList(
            "localhost:8080",
            "localhost:8081",
            "localhost:8082"
    );

    // API client
    private final ApiClient apiClient = new ApiClient();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Distributed Key-Value Store Manager");

        // Create main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");

        // Header
        HBox headerBox = createHeader();

        // Node selection
        HBox nodeBox = createNodeSelection();

        // Tab Pane for different operations
        TabPane tabPane = createTabPane();

        // Response area
        responseArea = new TextArea();
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setStyle("-fx-font-family: 'Monospace'; -fx-font-size: 12px;");
        responseArea.setPrefHeight(250);

        // Status bar
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Assemble main layout
        mainLayout.getChildren().addAll(
                headerBox,
                nodeBox,
                tabPane,
                new Label("Response:"),
                responseArea,
                new Separator(),
                statusLabel
        );

        // Load CSS
        Scene scene = new Scene(mainLayout, 900, 700);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial actions
        refreshClusterStatus();
    }

    private HBox createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label titleLabel = new Label("Distributed KV Store Manager");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button refreshButton = createStyledButton("⟳ Refresh All", "#3498db");
        refreshButton.setOnAction(e -> refreshAll());

        headerBox.getChildren().addAll(titleLabel, refreshButton);
        return headerBox;
    }

    private HBox createNodeSelection() {
        HBox nodeBox = new HBox(10);
        nodeBox.setAlignment(Pos.CENTER_LEFT);

        Label nodeLabel = new Label("Select Node:");
        nodeLabel.setStyle("-fx-font-weight: bold;");

        nodeSelector = new ComboBox<>();
        nodeSelector.getItems().addAll(nodes);
        nodeSelector.setValue(nodes.get(0));
        nodeSelector.setPrefWidth(200);

        Button refreshNodeButton = createStyledButton("↻", "#3498db");
        refreshNodeButton.setOnAction(e -> refreshClusterStatus());

        nodeBox.getChildren().addAll(nodeLabel, nodeSelector, refreshNodeButton);
        return nodeBox;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Cluster Status
        Tab clusterTab = new Tab("Cluster Status");
        clusterTab.setContent(createClusterTab());

        // Tab 2: Node Operations
        Tab operationsTab = new Tab("Node Operations");
        operationsTab.setContent(createOperationsTab());

        // Tab 3: Private APIs
        Tab privateTab = new Tab("Private APIs");
        privateTab.setContent(createPrivateTab());

        tabPane.getTabs().addAll(clusterTab, operationsTab, privateTab);
        return tabPane;
    }

    private VBox createClusterTab() {
        VBox clusterBox = new VBox(10);
        clusterBox.setPadding(new Insets(10));

        Button statusButton = createStyledButton("Get Cluster Status", "#2ecc71");
        statusButton.setOnAction(e -> refreshClusterStatus());

        Button keysButton = createStyledButton("Get Local Keys", "#e74c3c");
        keysButton.setOnAction(e -> getLocalKeys());

        clusterBox.getChildren().addAll(
                new Label("Cluster Management:"),
                createButtonRow(statusButton, keysButton)
        );

        return clusterBox;
    }

    private VBox createOperationsTab() {
        VBox opsBox = new VBox(15);
        opsBox.setPadding(new Insets(10));

        // Key input
        HBox keyBox = new HBox(10);
        keyBox.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label("Key:");
        keyLabel.setPrefWidth(50);

        keyField = new TextField();
        keyField.setPromptText("Enter key name");
        keyField.setPrefWidth(300);

        keyBox.getChildren().addAll(keyLabel, keyField);

        // Value input (for PUT)
        HBox valueBox = new HBox(10);
        valueBox.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label("Value:");
        valueLabel.setPrefWidth(50);

        valueField = new TextField();
        valueField.setPromptText("Enter value (for PUT)");
        valueField.setPrefWidth(300);

        valueBox.getChildren().addAll(valueLabel, valueField);

        // Operation buttons
        Button putButton = createStyledButton("PUT Key", "#3498db");
        putButton.setOnAction(e -> putKey());

        Button getButton = createStyledButton("GET Key", "#2ecc71");
        getButton.setOnAction(e -> getKey());

        Button deleteButton = createStyledButton("DELETE Key", "#e74c3c");
        deleteButton.setOnAction(e -> deleteKey());

        opsBox.getChildren().addAll(
                new Label("Key Operations:"),
                keyBox,
                valueBox,
                createButtonRow(putButton, getButton, deleteButton)
        );

        return opsBox;
    }

    private VBox createPrivateTab() {
        VBox privateBox = new VBox(15);
        privateBox.setPadding(new Insets(10));

        // Key input
        HBox keyBox = new HBox(10);
        keyBox.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label("Key:");
        keyLabel.setPrefWidth(50);

        TextField privateKeyField = new TextField();
        privateKeyField.setPromptText("Enter key name");
        privateKeyField.setPrefWidth(300);

        keyBox.getChildren().addAll(keyLabel, privateKeyField);

        // Operation buttons
        Button replicateButton = createStyledButton("Replicate to Node", "#9b59b6");
        replicateButton.setOnAction(e -> replicateKey(privateKeyField.getText()));

        Button deleteReplicateButton = createStyledButton("Delete Replication", "#e67e22");
        deleteReplicateButton.setOnAction(e -> deleteReplication(privateKeyField.getText()));

        privateBox.getChildren().addAll(
                new Label("Internal Operations:"),
                keyBox,
                createButtonRow(replicateButton, deleteReplicateButton)
        );

        return privateBox;
    }

    private HBox createButtonRow(Button... buttons) {
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getChildren().addAll(buttons);
        return buttonRow;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-padding: 8 15; -fx-background-radius: 5;",
                color
        ));

        button.setOnMouseEntered(e ->
                button.setStyle(String.format(
                        "-fx-background-color: derive(%s, -20%%); -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;",
                        color
                ))
        );

        button.setOnMouseExited(e ->
                button.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-padding: 8 15; -fx-background-radius: 5;",
                        color
                ))
        );

        return button;
    }

    // Action Methods

    private void refreshAll() {
        updateStatus("Refreshing all data...");
        refreshClusterStatus();
        updateStatus("Refresh complete");
    }

    private void refreshClusterStatus() {
        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.getClusterStatus(node);
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("Cluster status retrieved from " + node);
            });
        });
    }

    private void getLocalKeys() {
        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.getLocalKeys(node);
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("Local keys retrieved from " + node);
            });
        });
    }

    private void putKey() {
        if (keyField.getText().isEmpty()) {
            showAlert("Key is required");
            return;
        }

        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.putKey(node, keyField.getText(), valueField.getText());
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("PUT operation completed on " + node);
                keyField.clear();
                valueField.clear();
            });
        });
    }

    private void getKey() {
        if (keyField.getText().isEmpty()) {
            showAlert("Key is required");
            return;
        }

        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.getKey(node, keyField.getText());
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("GET operation completed on " + node);
            });
        });
    }

    private void deleteKey() {
        if (keyField.getText().isEmpty()) {
            showAlert("Key is required");
            return;
        }

        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.deleteKey(node, keyField.getText());
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("DELETE operation completed on " + node);
                keyField.clear();
                valueField.clear();
            });
        });
    }

    private void replicateKey(String key) {
        if (key.isEmpty()) {
            showAlert("Key is required");
            return;
        }

        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.replicateKey(node, key, valueField.getText());
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("Replication initiated on " + node);
            });
        });
    }

    private void deleteReplication(String key) {
        if (key.isEmpty()) {
            showAlert("Key is required");
            return;
        }

        executeAsync(() -> {
            String node = nodeSelector.getValue();
            String response = apiClient.deleteReplication(node, key);
            Platform.runLater(() -> {
                responseArea.setText(response);
                updateStatus("Delete replication completed on " + node);
            });
        });
    }

    private void executeAsync(Runnable task) {
        Task<Void> backgroundTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    task.run();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        responseArea.setText("Error: " + e.getMessage());
                        updateStatus("Operation failed: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        Thread thread = new Thread(backgroundTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

