package by.nexus.nexusdesktop.service.fxml;

import by.nexus.nexusdesktop.service.LoadSceneService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LoadFxmlSceneService implements LoadSceneService {

    private final FxmlLoaderFactory fxmlLoaderFactory;
    private final String templatePath;

    public LoadFxmlSceneService(FxmlLoaderFactory fxmlLoaderFactory,
                                @Value("${app.fx.template-path}") String templatePath) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.templatePath = templatePath;
    }

    @Override
    public Scene loadScene(String stageName) throws IOException {
        FXMLLoader fxmlLoader = fxmlLoaderFactory.createLoader(makeFxmlPath(stageName));
        return new Scene(fxmlLoader.load());
    }

    @Override
    public Scene loadScene(String stageName, double width, double height) throws IOException {
        FXMLLoader fxmlLoader = fxmlLoaderFactory.createLoader(makeFxmlPath(stageName));
        return new Scene(fxmlLoader.load(), width, height);
    }

    private String makeFxmlPath(String stageName) {
        String fxmlPath = templatePath + stageName;
        if (!fxmlPath.endsWith(".fxml")) {
            fxmlPath += ".fxml";
        }
        return fxmlPath;
    }
}
