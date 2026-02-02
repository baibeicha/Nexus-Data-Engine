package by.nexus.nexusdesktop.service.fxml;

import javafx.fxml.FXMLLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FxmlLoaderFactory {

    private final ApplicationContext applicationContext;

    public FXMLLoader createLoader(String fxmlName) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource(fxmlName));

        fxmlLoader.setControllerFactory(applicationContext::getBean);

        return fxmlLoader;
    }
}
