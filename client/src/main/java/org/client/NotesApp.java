package org.client;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.client.models.Note;
import org.client.controller.Controller;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.List;

public class NotesApp extends Application {
    private Controller controller = new Controller();
    private TableView<Note> notesTable;
    private ObservableList<Note> observableNotes;

    @Override
    public void start(Stage primaryStage) {
        if (controller.isLoggedIn()) {
            showMainGUI(primaryStage);
        } else {
            showLoginGUI(primaryStage);
        }
    }

    private void showLoginGUI(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField userField = new TextField();
        userField.setPromptText("Имя пользователя");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");
        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Зарегистрироваться");

        grid.add(new Label("Имя пользователя:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(registerButton, 1, 3);

        loginButton.setOnAction(e -> {
            if (controller.login(userField.getText(), passField.getText())) {
                showMainGUI(stage);
            } else {
                showAlert("Ошибка", "Неверные данные.");
            }
        });

        registerButton.setOnAction(e -> showRegisterGUI(stage));

        Scene scene = new Scene(grid, 300, 200);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle("Вход в систему заметок");
        stage.setScene(scene);
        stage.show();
    }

    private void showRegisterGUI(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Имя");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Фамилия");
        TextField userField = new TextField();
        userField.setPromptText("Имя пользователя");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");

        grid.add(new Label("Имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Фамилия:"), 0, 1);
        grid.add(surnameField, 1, 1);
        grid.add(new Label("Имя пользователя:"), 0, 2);
        grid.add(userField, 1, 2);
        grid.add(new Label("Пароль:"), 0, 3);
        grid.add(passField, 1, 3);
        grid.add(registerButton, 1, 4);
        grid.add(backButton, 0, 4);

        registerButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || surnameField.getText().isEmpty() ||
                    userField.getText().isEmpty() || passField.getText().isEmpty()) {
                showAlert("Ошибка", "Все поля должны быть заполнены.");
                return;
            }
            if (controller.register(nameField.getText(), surnameField.getText(), userField.getText(), passField.getText())) {
                showAlert("Успех", "Регистрация успешна. Теперь войдите.");
                showLoginGUI(stage);
            } else {
                showAlert("Ошибка", "Ошибка регистрации. Попробуйте другое имя пользователя.");
            }
        });

        backButton.setOnAction(e -> showLoginGUI(stage));

        Scene scene = new Scene(grid, 300, 250);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle("Регистрация");
        stage.setScene(scene);
        stage.show();
    }

    private void showMainGUI(Stage stage) {
        BorderPane root = new BorderPane();
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));

        Button createButton = new Button("Создать заметку");
        Button editButton = new Button("Редактировать");
        Button deleteButton = new Button("Удалить");
        CheckBox autoSyncCheck = new CheckBox("Авто-синхронизация");
        autoSyncCheck.setSelected(controller.isAutoSync());
        Button syncButton = new Button("Синхронизировать");
        Button logoutButton = new Button("Выйти");

        toolbar.getChildren().addAll(createButton, editButton, deleteButton, autoSyncCheck, syncButton, logoutButton);
        root.setTop(toolbar);

        notesTable = new TableView<>();
        TableColumn<Note, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Note, String> titleColumn = new TableColumn<>("Заголовок");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        notesTable.getColumns().addAll(idColumn, titleColumn);

        List<Note> notes = controller.getLocalNotes();
        observableNotes = FXCollections.observableArrayList(notes);
        notesTable.setItems(observableNotes);
        root.setCenter(notesTable);

        createButton.setOnAction(e -> showCreateNoteDialog());
        editButton.setOnAction(e -> {
            Note selected = notesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditNoteDialog(selected);
            } else {
                showAlert("Ошибка", "Выберите заметку.");
            }
        });
        deleteButton.setOnAction(e -> {
            Note selected = notesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Подтверждение");
                confirm.setHeaderText("Удалить заметку?");
                confirm.setContentText(selected.getTitle());
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    controller.deleteNote(selected.getId());
                    observableNotes.setAll(controller.getLocalNotes());
                }
            } else {
                showAlert("Ошибка", "Выберите заметку.");
            }
        });
        autoSyncCheck.setOnAction(e -> controller.setAutoSync(autoSyncCheck.isSelected()));
        syncButton.setOnAction(e -> showSyncDialog());
        logoutButton.setOnAction(e -> {
            controller.logout();
            showLoginGUI(stage);
        });

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle("Система заметок - " + controller.getCurrentUser().getName());
        stage.setScene(scene);
        stage.show();
    }

    private void showCreateNoteDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создать заметку");
        TextField titleField = new TextField();
        titleField.setPromptText("Заголовок");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Содержание");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Содержание:"), 0, 1);
        grid.add(contentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Анимация появления
        dialog.getDialogPane().setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), dialog.getDialogPane());
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
                    showAlert("Ошибка", "Заголовок и содержание не могут быть пустыми.");
                    return null;
                }
                controller.createNote(titleField.getText(), contentArea.getText());
                observableNotes.setAll(controller.getLocalNotes());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditNoteDialog(Note note) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактировать заметку");
        TextField titleField = new TextField(note.getTitle());
        TextArea contentArea = new TextArea(note.getText());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Заголовок:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Содержание:"), 0, 1);
        grid.add(contentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Анимация появления
        dialog.getDialogPane().setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), dialog.getDialogPane());
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
                    showAlert("Ошибка", "Заголовок и содержание не могут быть пустыми.");
                    return null;
                }
                controller.updateNote(note.getId(), titleField.getText(), contentArea.getText());
                observableNotes.setAll(controller.getLocalNotes());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showSyncDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Синхронизация");
        ChoiceBox<String> syncChoice = new ChoiceBox<>();
        syncChoice.getItems().addAll("Заменить локальные заметки серверными", "Загрузить локальные заметки на сервер");
        syncChoice.setValue("Заменить локальные заметки серверными");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Выберите действие:"), 0, 0);
        grid.add(syncChoice, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Анимация появления
        dialog.getDialogPane().setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), dialog.getDialogPane());
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (syncChoice.getValue().equals("Заменить локальные заметки серверными")) {
                    controller.syncReplaceLocalWithServer();
                } else {
                    controller.syncUploadLocalToServer();
                }
                observableNotes.setAll(controller.getLocalNotes());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.getDialogPane().setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(500), alert.getDialogPane());
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        alert.showAndWait();
    }

    @Override
    public void stop() {
        controller.saveToFile();
    }

    public static void main(String[] args) {
        launch(args);
    }
}