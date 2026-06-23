package se.viktor.fancontrollerlinux.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import se.viktor.fancontrollerlinux.model.FanSensor;
import se.viktor.fancontrollerlinux.service.HwmonService;
import se.viktor.fancontrollerlinux.ui.util.ColorScale;

/**
 * Glass card showing one fan's RPM and a PWM control slider.
 *
 *   ⟳  CPU Fan          1240 RPM
 *      nct6775
 *   RPM  ████░░░░  62%
 *   PWM  ──●────   55%
 */
public class FanCard extends VBox {

    private static final int MAX_RPM = 2000;

    private final Label       nameLabel;
    private final Label       driverLabel;
    private final Label       rpmValueLabel;
    private final ProgressBar rpmBar;
    private final Label       rpmPctLabel;
    private final Slider      pwmSlider;
    private final Label       pwmPctLabel;
    private final Label       warningLabel;

    private String hwmonName;
    private int    pwmIndex;

    public FanCard(HwmonService hwmon) {
        super(10);
        getStyleClass().add("fan-card");
        setPadding(new Insets(16));

        // ── Header ────────────────────────────────────────────────────────────
        Label spinIcon = new Label("⟳");
        spinIcon.getStyleClass().add("fan-spin");

        nameLabel   = new Label("—");
        nameLabel.getStyleClass().add("fan-name");
        driverLabel = new Label("—");
        driverLabel.getStyleClass().add("driver-label");
        VBox nameBox = new VBox(2, nameLabel, driverLabel);

        rpmValueLabel = new Label("—  RPM");
        rpmValueLabel.getStyleClass().add("rpm-value");

        HBox header = new HBox(10, spinIcon, nameBox, rpmValueLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // ── RPM bar ───────────────────────────────────────────────────────────
        rpmBar = new ProgressBar(0);
        rpmBar.getStyleClass().add("rpm-bar");
        rpmBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rpmBar, Priority.ALWAYS);

        rpmPctLabel = new Label("0%");
        rpmPctLabel.getStyleClass().add("pct-label");
        rpmPctLabel.setMinWidth(32);

        HBox rpmRow = barRow("RPM", rpmBar, rpmPctLabel);

        // ── PWM slider ────────────────────────────────────────────────────────
        pwmSlider = new Slider(20, 100, 50);
        pwmSlider.getStyleClass().add("pwm-slider");
        HBox.setHgrow(pwmSlider, Priority.ALWAYS);

        pwmPctLabel = new Label("50%");
        pwmPctLabel.getStyleClass().add("pct-label");
        pwmPctLabel.setMinWidth(32);

        pwmSlider.valueProperty().addListener((obs, old, val) ->
            pwmPctLabel.setText(val.intValue() + "%")
        );

        // Write to hwmon when user releases slider
        pwmSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && hwmonName != null) {
                hwmon.setPwmPercent(hwmonName, pwmIndex, (int) pwmSlider.getValue());
            }
        });

        HBox pwmRow = barRow("PWM", pwmSlider, pwmPctLabel);

        // ── Warning ───────────────────────────────────────────────────────────
        warningLabel = new Label("⚠  Kräver root för PWM-kontroll");
        warningLabel.getStyleClass().add("warning-label");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);

        getChildren().addAll(header, rpmRow, pwmRow, warningLabel);
    }

    public void refresh(FanSensor fan) {
        // Parse "hwmon2_fan1" → hwmonName="hwmon2", pwmIndex=1
        String[] parts = fan.id().split("_fan");
        if (parts.length == 2) {
            hwmonName = parts[0];
            pwmIndex  = Integer.parseInt(parts[1]);
        }

        nameLabel.setText(fan.label());
        driverLabel.setText(fan.driver());
        rpmValueLabel.setText(fan.rpm() + "  RPM");

        double rpmFraction = Math.min(fan.rpm() / (double) MAX_RPM, 1.0);
        rpmBar.setProgress(rpmFraction);
        rpmPctLabel.setText((int)(rpmFraction * 100) + "%");

        if (!pwmSlider.isValueChanging()) {
            pwmSlider.setValue(fan.pwmPercent());
        }
        pwmSlider.setDisable(!fan.pwmWritable());

        // Update slider thumb color via inline style
        String color = ColorScale.forPwmHex(fan.pwmPercent());
        pwmSlider.lookup(".thumb");   // force CSS pass
        pwmSlider.setStyle("-fx-accent: " + color + ";");

        warningLabel.setVisible(!fan.pwmWritable());
        warningLabel.setManaged(!fan.pwmWritable());
    }

    private HBox barRow(String tag, javafx.scene.Node control, Label pctLabel) {
        Label tagLabel = new Label(tag);
        tagLabel.getStyleClass().add("bar-tag");
        tagLabel.setMinWidth(32);
        pctLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox row = new HBox(8, tagLabel, control, pctLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
