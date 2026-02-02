package by.nexus.nexusdesktop.controller;

import by.nexus.nexusdesktop.annotation.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

@FxController
public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}