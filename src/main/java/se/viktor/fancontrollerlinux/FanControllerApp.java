package se.viktor.fancontrollerlinux;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import se.viktor.fancontrollerlinux.config.AppConfig;
import se.viktor.fancontrollerlinux.service.HwmonService;
import se.viktor.fancontrollerlinux.ui.MainView;

public class FanControllerApp extends Application {

    @Override
    public void start(Stage stage) {
        AppConfig    config = new AppConfig();
        HwmonService hwmon  = new HwmonService();
        MainView     view   = new MainView(hwmon, stage, config);

        Scene scene = new Scene(view.getRoot());
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            getClass().getResource("app.css").toExternalForm()
        );

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Fan Controller Linux");
        stage.setScene(scene);

        // Restore last window position
        stage.setX(config.getWindowX());
        stage.setY(config.getWindowY());
        stage.show();

        // Save position when window moves
        stage.xProperty().addListener((obs, o, n) -> config.setWindowX(n.doubleValue()));
        stage.yProperty().addListener((obs, o, n) -> config.setWindowY(n.doubleValue()));

        // Initial data load
        view.refresh();

        // Poll hwmon every 2 seconds
        Timeline poll = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> view.refresh())
        );
        poll.setCycleCount(Timeline.INDEFINITE);
        poll.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
