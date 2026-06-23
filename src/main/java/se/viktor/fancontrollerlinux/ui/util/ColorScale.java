package se.viktor.fancontrollerlinux.ui.util;

import javafx.scene.paint.Color;

/**
 * Centralized color logic so TempGauge and FanCard
 * always use the same temperature/PWM color scale.
 */
public final class ColorScale {

    private ColorScale() {}

    /** Green → yellow → orange → red based on temperature. */
    public static Color forTemp(double celsius) {
        if (celsius >= 80) return Color.web("#ff453a");
        if (celsius >= 65) return Color.web("#ff9f0a");
        if (celsius >= 50) return Color.web("#ffd60a");
        return Color.web("#30d158");
    }

    /** Blue → orange → red based on PWM percent. */
    public static Color forPwm(int percent) {
        if (percent > 80) return Color.web("#ff453a");
        if (percent > 50) return Color.web("#ff9f0a");
        return Color.web("#0a84ff");
    }

    /** CSS hex string version of forPwm — for JavaFX CSS -fx-accent etc. */
    public static String forPwmHex(int percent) {
        if (percent > 80) return "#ff453a";
        if (percent > 50) return "#ff9f0a";
        return "#0a84ff";
    }
}
