package by.nexus.nexusdesktop.service;

import javafx.scene.Scene;

import java.io.IOException;

public interface LoadSceneService {
    Scene loadScene(String stageName) throws IOException;
    Scene loadScene(String stageName, double width, double height) throws IOException;
}
