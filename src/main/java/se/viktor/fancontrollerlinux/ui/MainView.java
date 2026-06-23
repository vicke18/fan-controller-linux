package se.viktor.fancontrollerlinux.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import se.viktor.fancontrollerlinux.config.AppConfig;
import se.viktor.fancontrollerlinux.model.FanSensor;
import se.viktor.fancontrollerlinux.model.TempSensor;
import se.viktor.fancontrollerlinux.service.HwmonService;
import se.viktor.fancontrollerlinux.ui.components.FanCard;
import se.viktor.fancontrollerlinux.ui.components.HeaderBar;
import se.viktor.fancontrollerlinux.ui.components.TempGauge;

import java.util.ArrayList;
import java.util.List;

public class MainView {

    private final VBox         root;
    private final FlowPane     tempPane;
    private final VBox         fanPane;
    private final HwmonService hwmon;

    private final List<TempGauge> gauges   = new ArrayList<>();
    private final List<FanCard>   fanCards = new ArrayList<>();

    public MainView(HwmonService hwmon, Stage stage, AppConfig config) {
        this.hwmon = hwmon;

        HeaderBar header = new HeaderBar(stage);

        // Temperature section
        Label tempTitle = sectionTitle("▲  TEMPERATURER");
        tempPane = new FlowPane(10, 10);
        tempPane.setPrefWrapLength(460);

        // Fan section
        Label fanTitle = sectionTitle("◎  FLÄKTAR");
        fanPane = new VBox(10);

        // Glass panel wrapping everything
        VBox panel = new VBox(16, header, tempTitle, tempPane, fanTitle, fanPane);
        panel.getStyleClass().add("glass-panel");
        panel.setPadding(new Insets(22));
        panel.setMinWidth(480);
        panel.setMaxWidth(480);

        // ScrollPane so all content is reachable
        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Transparent root — padding gives the dropshadow room to render
        root = new VBox(scrollPane);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(12));
        root.setMinWidth(504);
        root.setMinHeight(400);
        root.setMaxHeight(700);
    }

    /** Called by FanControllerApp's Timeline every 2 seconds. */
    public void refresh() {
        List<TempSensor> temps = hwmon.readTemps();
        List<FanSensor>  fans  = hwmon.readFans();

        syncTempGauges(temps);
        syncFanCards(fans);
    }

    public VBox getRoot() { return root; }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void syncTempGauges(List<TempSensor> temps) {
        if (temps.size() != gauges.size()) {
            gauges.clear();
            tempPane.getChildren().clear();
            for (TempSensor t : temps) {
                TempGauge gauge = new TempGauge();
                Label     lbl   = new Label(t.label());
                Label     drv   = new Label(t.driver());
                lbl.getStyleClass().add("temp-label");
                drv.getStyleClass().add("driver-label");
                VBox box = new VBox(4, gauge, lbl, drv);
                box.setAlignment(Pos.TOP_CENTER);
                box.getStyleClass().add("temp-card");
                box.setPadding(new Insets(12, 10, 12, 10));
                tempPane.getChildren().add(box);
                gauges.add(gauge);
            }
        }
        for (int i = 0; i < temps.size(); i++) {
            TempSensor t   = temps.get(i);
            TempGauge  g   = gauges.get(i);
            VBox       box = (VBox) tempPane.getChildren().get(i);
            g.update(t.tempCelsius());
            ((Label) box.getChildren().get(1)).setText(t.label());
            ((Label) box.getChildren().get(2)).setText(t.driver());
        }
    }

    private void syncFanCards(List<FanSensor> fans) {
        if (fans.size() != fanCards.size()) {
            fanCards.clear();
            fanPane.getChildren().clear();
            for (int i = 0; i < fans.size(); i++) {
                FanCard card = new FanCard(hwmon);
                fanCards.add(card);
                fanPane.getChildren().add(card);
            }
        }
        for (int i = 0; i < fans.size(); i++) {
            fanCards.get(i).refresh(fans.get(i));
        }
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }
}