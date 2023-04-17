package client;

import server.models.Course;
import server.models.RegistrationForm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;

import java.util.ArrayList;
import java.util.InputMismatchException;

/**
 * La classe ClientCommand est une interface de graphique qui permet à un utilisateur de consulter les cours d'une
 * session et de s'inscrire à un cours.
 */
public class ClientGraphic extends Application {
    private static final String IP = "127.0.0.1";
    private static final int PORT = 1337;
    static Client client;
    private static Course selectedCourse;
    private String selectedSession;
    private static TextField firstNameField, lastNameField, emailNameField, matriculeField;

    /**
     * La méthode start définit la forme de l'interface graphique et des évènements qui y sont liés.
     * @param primaryStage la fenêtre principale de cette application, sur laquelle la scène de l'application peut être
     *                     définie. Les applications peuvent créer d'autres fenêtres si nécessaire, mais elles ne
     *                     seront pas des fenêtres principales.
     */
    @Override
    public void start(Stage primaryStage) {
        VBox leftVBox = new VBox();
        leftVBox.setAlignment(Pos.TOP_CENTER);
        leftVBox.setSpacing(10);
        leftVBox.setPadding(new Insets(0, 20, 20, 20));

        HBox leftTitleHbox = new HBox();
        leftTitleHbox.setAlignment(Pos.CENTER);
        Label leftTitle = new Label("Liste des cours");
        leftTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        leftTitleHbox.getChildren().add(leftTitle);
        leftVBox.getChildren().add(leftTitleHbox);

        TableView<Course> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setEditable(false);
        TableColumn<Course, String> col1 = new TableColumn<>("Code");
        col1.setCellValueFactory(new PropertyValueFactory<Course, String>("code"));
        TableColumn<Course, String> col2 = new TableColumn<>("Cours");
        col2.setCellValueFactory(new PropertyValueFactory<Course, String>("name"));
        table.getColumns().addAll(col1, col2);
        leftVBox.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setOnMouseClicked(event -> {
            selectedCourse = table.getSelectionModel().getSelectedItem();
        });

        HBox leftInputsHbox = new HBox();
        leftInputsHbox.setAlignment(Pos.CENTER);
        leftInputsHbox.setSpacing(75);
        leftInputsHbox.setPadding(new Insets(20, 0, 20, 0));

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Automne", "Hiver", "Ete");
        comboBox.setValue("Automne"); comboBox.setStyle("-fx-font-size: 15px;");
        selectedSession = comboBox.getValue();
        comboBox.setOnAction(e -> {selectedSession = comboBox.getValue();});

        Button load = new Button("Charger");
        load.setStyle("-fx-font-size: 15px;");
        load.setOnAction(e -> {
            selectedCourse = null;
            client.connect();
            ArrayList<Course> courses = (ArrayList<Course>) client.loadCourses(selectedSession);
            table.setItems((ObservableList<Course>) FXCollections.observableArrayList(courses));
        });

        leftInputsHbox.getChildren().addAll(comboBox, load);
        leftVBox.getChildren().add(leftInputsHbox);

        VBox rightVBox = new VBox();
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setSpacing(40);
        rightVBox.setPadding(new Insets(0, 40, 20, 40));

        HBox rightTitleHbox = new HBox();
        rightTitleHbox.setAlignment(Pos.CENTER);
        Label rightTitle = new Label("Formulaire d'inscription");
        rightTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        rightTitleHbox.getChildren().add(rightTitle);
        rightVBox.getChildren().add(rightTitleHbox);

        VBox form = new VBox();
        form.setAlignment(Pos.CENTER);
        form.setSpacing(10);
        HBox firstNameHBox = new HBox();
        HBox lastNameHBox = new HBox();
        HBox emailNameHBox = new HBox();
        HBox matriculeNameHBox = new HBox();

        Label firstNameLabel = new Label("Prenom"); firstNameLabel.setPrefWidth(100);
        firstNameLabel.setStyle("-fx-font-size: 15px;");
        Label lastNameLabel = new Label("Nom"); lastNameLabel.setPrefWidth(100);
        lastNameLabel.setStyle("-fx-font-size: 15px;");
        Label emailNameLabel = new Label("Email"); emailNameLabel.setPrefWidth(100);
        emailNameLabel.setStyle("-fx-font-size: 15px;");
        Label matriculeLabel = new Label("Matricule"); matriculeLabel.setPrefWidth(100);
        matriculeLabel.setStyle("-fx-font-size: 15px;");

        firstNameField = new TextField();
        lastNameField = new TextField();
        emailNameField = new TextField();
        matriculeField = new TextField();

        firstNameHBox.getChildren().addAll(firstNameLabel, firstNameField);
        HBox.setHgrow(firstNameLabel, Priority.ALWAYS); HBox.setHgrow(firstNameField, Priority.ALWAYS);
        lastNameHBox.getChildren().addAll(lastNameLabel, lastNameField);
        HBox.setHgrow(lastNameLabel, Priority.ALWAYS); HBox.setHgrow(lastNameField, Priority.ALWAYS);
        emailNameHBox.getChildren().addAll(emailNameLabel, emailNameField);
        HBox.setHgrow(emailNameLabel, Priority.ALWAYS); HBox.setHgrow(emailNameField, Priority.ALWAYS);
        matriculeNameHBox.getChildren().addAll(matriculeLabel, matriculeField);
        HBox.setHgrow(matriculeLabel, Priority.ALWAYS); HBox.setHgrow(matriculeField, Priority.ALWAYS);

        HBox sendHBox = new HBox();
        sendHBox.setAlignment(Pos.CENTER);
        sendHBox.setPadding(new Insets(20, 0, 0, 100));

        Button send = new Button("Envoyer");
        send.setStyle("-fx-font-size: 15px;");
        send.setOnAction(event -> sendRegistrationForm());

        sendHBox.getChildren().add(send);
        form.getChildren().addAll(firstNameHBox, lastNameHBox, emailNameHBox, matriculeNameHBox, sendHBox);
        rightVBox.getChildren().add(form);

        HBox root = new HBox(leftVBox, rightVBox);
        HBox.setHgrow(leftVBox, Priority.ALWAYS);
        HBox.setHgrow(rightVBox, Priority.ALWAYS);

        Scene scene = new Scene(root, 750, 550);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("IFT1025 - TP2");
        primaryStage.show();
    }

    /**
     * La méthode main est le point d'entrée du programme. Elle permet d'afficher l'interface graphique.
     * @param args les arguments en ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try {
            client = new Client(IP, PORT);
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * La méthode sendRegistration vérifie que tout les informations entrées par l'utilisateur sont correctes. En
     * fonction, elle fait apparaître une alerte d'erreur ou d'information.
     */
    public static void sendRegistrationForm() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailNameField.getText();
        String matricule = matriculeField.getText();
        RegistrationForm registrationForm = new RegistrationForm(
                firstName, lastName, email, matricule, selectedCourse);
        try {
            if(!client.verifyMatricule(matricule) || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    selectedCourse == null) throw new InputMismatchException();
            Alert alertInfo = new Alert(AlertType.INFORMATION);
            alertInfo.setContentText("Félicitations ! Inscription réussie de " + firstName + " " + lastName +
                    " au cours " + selectedCourse.getCode() + ".");
            alertInfo.showAndWait();
            client.registration(registrationForm);
        } catch(InputMismatchException e) {
            Alert alert = new Alert(AlertType.ERROR);
            String alertText = "Le formulaire est invalide.\n";
            if(selectedCourse == null) alertText += "Vous devez selectionner un cours !\n";
            if(firstName.isEmpty()) alertText += "Le champ 'Prénom' est invalide !\n";
            if(lastName.isEmpty()) alertText += "Le champ 'Nom' est invalide !\n";
            if(email.isEmpty()) alertText += "Le champ 'Email' est invalide !\n";
            if(!client.verifyMatricule(matricule)) alertText += "Le champ 'Matricule' est invalide !\n";

            alert.setContentText(alertText);
            alert.showAndWait();
        }
    }
}