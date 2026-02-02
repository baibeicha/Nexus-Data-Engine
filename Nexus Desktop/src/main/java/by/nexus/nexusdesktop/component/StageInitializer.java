package by.nexus.nexusdesktop.component;

import by.nexus.nexusdesktop.event.StageReadyEvent;
import by.nexus.nexusdesktop.service.LoadSceneService;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final String applicationTitle;
    private final String startPage;
    private final LoadSceneService loadSceneService;

    public StageInitializer(@Value("${spring.application.name}") String applicationTitle,
                            @Value("${app.fx.start-page}") String startPage,
                            LoadSceneService loadSceneService) {
        this.applicationTitle = applicationTitle;
        this.startPage = startPage;
        this.loadSceneService = loadSceneService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        loadFxml(event.getStage());
    }

    private void loadFxml(Stage stage) {
        try {
            Scene scene = loadSceneService.loadScene(startPage, 600, 800);
            stage.setTitle(applicationTitle);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
