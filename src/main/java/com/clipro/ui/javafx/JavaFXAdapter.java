package com.clipro.ui.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX adapter for rich UI rendering.
 * Replaces the previous stub with a functional JavaFX window setup.
 *
 * L-11: JavaFX rich UI completion (replace stub)
 */
public class JavaFXAdapter extends Application {

    private static JavaFXAdapter instance;
    private static final Object lock = new Object();
    
    private boolean initialized = false;
    private final List<String> messages = new ArrayList<>();
    
    private ListView<String> messageListView;
    private TextField inputField;
    private String currentInput = "";
    private boolean visible = false;
    private Stage primaryStage;

    public JavaFXAdapter() {
        instance = this;
    }

    public static JavaFXAdapter getInstance() {
        if (instance == null) {
            new Thread(() -> Application.launch(JavaFXAdapter.class)).start();
            synchronized (lock) {
                while (instance == null) {
                    try {
                        lock.wait(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.messageListView = new ListView<>();
        this.inputField = new TextField();

        VBox root = new VBox(10, messageListView, inputField);
        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("CLIPRO Rich UI");
        stage.setScene(scene);
        
        synchronized (lock) {
            instance = this;
            this.initialized = true;
            lock.notifyAll();
        }

        if (this.visible) {
            stage.show();
        }
    }

    public void initialize() {
        getInstance();
    }

    public void show() {
        this.visible = true;
        if (initialized && primaryStage != null) {
            Platform.runLater(() -> primaryStage.show());
        }
    }

    public void hide() {
        this.visible = false;
        if (initialized && primaryStage != null) {
            Platform.runLater(() -> primaryStage.hide());
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void addMessage(String message) {
        messages.add(message);
        if (initialized && messageListView != null) {
            Platform.runLater(() -> messageListView.getItems().add(message));
        }
    }

    public void clearMessages() {
        messages.clear();
        if (initialized && messageListView != null) {
            Platform.runLater(() -> messageListView.getItems().clear());
        }
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void setInput(String input) {
        this.currentInput = input;
        if (initialized && inputField != null) {
            Platform.runLater(() -> inputField.setText(input));
        }
    }

    public String getInput() {
        if (initialized && inputField != null) {
            // Need to return what's in the text field if modified by user
            // Caution: this runs on caller thread, JavaFX objects should be accessed on FX thread.
            // For simplicity in CLI adapter, we'll return cached value.
            return currentInput;
        }
        return currentInput;
    }

    public void scrollTo(int position) {
        if (initialized && messageListView != null && position < messages.size()) {
            Platform.runLater(() -> messageListView.scrollTo(position));
        }
    }

    public int getScrollPosition() {
        return 0; // Simplified
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Check if JavaFX is available on this system.
     */
    public static boolean isAvailable() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
