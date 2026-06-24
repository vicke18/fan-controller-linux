package se.viktor.fancontrollerlinux.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import se.viktor.fancontrollerlinux.config.AppConfig;
import se.viktor.fancontrollerlinux.model.FanSensor;
import se.viktor.fancontrollerlinux.model.TempSensor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A dialog with checkboxes for toggling which sensors are visible.
 * Opens as a separate window when the ⚙ button is clicked.
 * Saves choices to AppConfig (persisted between sessions).
 */
public class FilterDialog {

    private final AppConfig config;
    private Runnable onSave;  // callback so MainView can refresh after save

    public FilterDialog(AppConfig config) {
        this.config = config;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void show(Stage owner, List<TempSensor> allTemps, List<FanSensor> allFans) {
        Set<String> hidden = config.getHiddenSensors();

        // ── Build content ─────────────────────────────────────────────────────

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle(
            "-fx-background-color: rgba(10,14,30,0.97);" +
            "-fx-background-radius: 14;"
        );

        // Title
        Label title = new Label("⚙  Välj sensorer");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        // ── Temperature checkboxes ────────────────────────────────────────────
        Label tempHeader = sectionLabel("▲  TEMPERATURER");
        VBox tempBox = new VBox(6);
        for (TempSensor t : allTemps) {
            CheckBox cb = new CheckBox(t.label() + "  (" + t.driver() + ")");
            cb.setSelected(!hidden.contains(t.id()));
            cb.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;");
            cb.setUserData(t.id());
            tempBox.getChildren().add(cb);
        }

        // ── Fan checkboxes ────────────────────────────────────────────────────
        Label fanHeader = sectionLabel("◎  FLÄKTAR");
        VBox fanBox = new VBox(6);
        for (FanSensor f : allFans) {
            CheckBox cb = new CheckBox(f.label() + "  (" + f.driver() + ")");
            cb.setSelected(!hidden.contains(f.id()));
            cb.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;");
            cb.setUserData(f.id());
            fanBox.getChildren().add(cb);
        }

        // ── Save button ───────────────────────────────────────────────────────
        Button saveBtn = new Button("Spara");
        saveBtn.setStyle(
            "-fx-background-color: #0a84ff; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-background-radius: 8;" +
            "-fx-padding: 8 24 8 24; -fx-cursor: hand;"
        );

        Stage dialog = new Stage();

        saveBtn.setOnAction(e -> {
            Set<String> newHidden = new HashSet<>();
            // Collect unchecked temp sensors
            for (javafx.scene.Node node : tempBox.getChildren()) {
                if (node instanceof CheckBox cb && !cb.isSelected()) {
                    newHidden.add((String) cb.getUserData());
                }
            }
            // Collect unchecked fan sensors
            for (javafx.scene.Node node : fanBox.getChildren()) {
                if (node instanceof CheckBox cb && !cb.isSelected()) {
                    newHidden.add((String) cb.getUserData());
                }
            }
            config.setHiddenSensors(newHidden);
            if (onSave != null) onSave.run();
            dialog.close();
        });

        HBox btnRow = new HBox(saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(
            title,
            tempHeader, tempBox,
            fanHeader, fanBox,
            btnRow
        );

        // ── Stage setup ───────────────────────────────────────────────────────
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Scene scene = new Scene(scroll, 340, 500);
        scene.setFill(Color.TRANSPARENT);

        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Filtrera sensorer");
        dialog.setScene(scene);
        dialog.setAlwaysOnTop(true);
        dialog.show();
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.3);" +
            "-fx-font-size: 9px; -fx-font-weight: bold;"
        );
        return l;
    }
}
