package se.viktor.fancontrollerlinux.ui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * The draggable header bar:
 *   ◈  Fan Controller Linux      [−] [×]
 *
 * Extracted from MainView so it owns its own drag logic and window buttons.
 */
public class HeaderBar extends HBox {

    private double dragX, dragY;

    public HeaderBar(Stage stage) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 0, 14, 0));

        // Logo + title
        Label logo = new Label("◈");
        logo.getStyleClass().add("logo-icon");

        Label title = new Label("Fan Controller Linux");
        title.getStyleClass().add("logo-title");
        Label sub = new Label("hwmon · sysfs");
        sub.getStyleClass().add("logo-sub");

        var titleBox = new javafx.scene.layout.VBox(1, title, sub);

        // Spacer pushes buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Window buttons
        Label btnMin = new Label("−");
        btnMin.getStyleClass().add("win-btn");
        btnMin.setOnMouseClicked(e -> stage.setIconified(true));

        Label btnClose = new Label("×");
        btnClose.getStyleClass().addAll("win-btn", "win-close");
        btnClose.setOnMouseClicked(e -> Platform.exit());

        getChildren().addAll(logo, titleBox, spacer, btnMin, btnClose);

        // Dragging moves the window
        setOnMousePressed(e -> {
            dragX = e.getSceneX();
            dragY = e.getSceneY();
        });
        setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragX);
            stage.setY(e.getScreenY() - dragY);
        });
    }
}
