package com.dor_cohen.ui;

import com.dor_cohen.app.dm.Book;
import com.dor_cohen.network.dto.Request;
import com.dor_cohen.network.dto.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class LibraryApp extends Application {
    private TableView<Book> table;
    private TextField idField;
    private TextField titleField;
    private TextField authorField;
    private final Gson gson = new Gson();

    @Override
    public void start(Stage stage) {
        // --- WELCOME PANE ---
        StackPane welcomePane = new StackPane();
        // Load background image (absolute file path)
        String imgPath = "file:D:/המחשב שלי/הורדות/BooksLibraryFull/BooksLibraryFull/library.jpg";
        Image bgImage = new Image(imgPath);
        BackgroundImage bgi = new BackgroundImage(bgImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false));
        welcomePane.setBackground(new Background(bgi));

        Label welcomeLabel = new Label("Welcome to the Books Library App!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        Button startBtn = new Button("Enter");
        startBtn.setOnAction(e -> stage.setScene(buildMainScene(stage)));

        VBox welcomeBox = new VBox(20, welcomeLabel, startBtn);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomePane.getChildren().add(welcomeBox);

        Scene welcomeScene = new Scene(welcomePane, 800, 400);
        stage.setTitle("Books Library");
        stage.setScene(welcomeScene);
        stage.show();
    }

    /**
     * Constructs the main library scene.
     */
    private Scene buildMainScene(Stage stage) {
        // Table setup
        table = new TableView<>();
        TableColumn<Book, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        table.getColumns().addAll(colId, colTitle, colAuthor);

        // Input fields
        idField = new TextField();   idField.setPromptText("ID");
        titleField = new TextField(); titleField.setPromptText("Title");
        authorField = new TextField();authorField.setPromptText("Author");

        // Buttons: Get All, Get, Add, Clear, Delete
        Button refreshBtn = new Button("Get All");
        Button getBtn     = new Button("Get");
        Button addBtn     = new Button("Add");
        Button clearBtn   = new Button("Clear");
        Button delBtn     = new Button("Delete");
        delBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        // Actions
        refreshBtn.setOnAction(e -> loadBooks());
        getBtn.setOnAction(e -> sendRequest("get"));
        addBtn.setOnAction(e -> sendRequest("add"));
        clearBtn.setOnAction(e -> table.getItems().clear());
        delBtn.setOnAction(e -> sendRequest("delete"));

        // Populate fields when selecting row
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                idField.setText(String.valueOf(newSel.getId()));
                titleField.setText(newSel.getTitle());
                authorField.setText(newSel.getAuthor());
            }
        });

        HBox form = new HBox(10,
                idField, titleField, authorField,
                refreshBtn, getBtn, addBtn, clearBtn, delBtn
        );
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane(table, null, null, form, null);
        return new Scene(root, 800, 400);
    }

    private void loadBooks() {
        try (Socket socket = new Socket("localhost", 34567);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            Request<Void> req = new Request<>();
            req.action = "getAll";
            req.body = null;
            writer.println(gson.toJson(req));

            String respJson = reader.readLine();
            Type type = new TypeToken<Response<List<Book>>>(){}.getType();
            Response<List<Book>> resp = gson.fromJson(respJson, type);
            List<Book> list = resp != null && resp.data != null ? resp.data : Collections.emptyList();
            table.setItems(FXCollections.observableArrayList(list));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendRequest(String action) {
        // Duplicate-ID check on add
        if (action.equals("add")) {
            try {
                long newId = Long.parseLong(idField.getText());
                if (table.getItems().stream().anyMatch(b -> b.getId().equals(newId))) {
                    Alert dup = new Alert(Alert.AlertType.WARNING);
                    dup.setTitle("Duplicate ID");
                    dup.setHeaderText("Cannot add book");
                    dup.setContentText("A book with ID " + newId + " already exists.");
                    dup.showAndWait();
                    return;
                }
            } catch(NumberFormatException nfe) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Invalid ID");
                err.setHeaderText("Invalid input");
                err.setContentText("Please enter a valid numeric ID.");
                err.showAndWait();
                return;
            }
        }

        try (Socket socket = new Socket("localhost", 34567);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            Book book;
            if (action.equals("get") || action.equals("delete")) {
                Book sel = table.getSelectionModel().getSelectedItem();
                book = sel!=null
                        ? new Book(sel.getId(), null, null)
                        : new Book(Long.parseLong(idField.getText()), null, null);
            } else {
                book = new Book(
                        Long.parseLong(idField.getText()),
                        titleField.getText(),
                        authorField.getText()
                );
            }

            Request<Book> req = new Request<>();
            req.action = action;
            req.body   = book;
            writer.println(gson.toJson(req));

            String resp = reader.readLine();
            if (action.equals("get")) {
                Type t2 = new TypeToken<Response<Book>>(){}.getType();
                Response<Book> r2 = gson.fromJson(resp, t2);
                if (r2!=null && r2.data!=null) table.setItems(FXCollections.observableArrayList(r2.data));
                else table.getItems().clear();
            } else {
                loadBooks();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
