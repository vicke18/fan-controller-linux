package se.viktor.fancontrollerlinux.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

public class AppConfig {

    private static final String KEY_WIN_X        = "window.x";
    private static final String KEY_WIN_Y        = "window.y";
    private static final String KEY_POLL_SECONDS = "poll.seconds";
    private static final String KEY_MAX_RPM      = "max.rpm";
    private static final String KEY_HIDDEN       = "hidden.sensors";

    private final Preferences prefs =
        Preferences.userNodeForPackage(AppConfig.class);

    // ── Window position ───────────────────────────────────────────────────────

    public double getWindowX()              { return prefs.getDouble(KEY_WIN_X, 40); }
    public double getWindowY()              { return prefs.getDouble(KEY_WIN_Y, 40); }
    public void   setWindowX(double x)     { prefs.putDouble(KEY_WIN_X, x); }
    public void   setWindowY(double y)     { prefs.putDouble(KEY_WIN_Y, y); }

    // ── Poll interval ─────────────────────────────────────────────────────────

    public int  getPollSeconds()            { return prefs.getInt(KEY_POLL_SECONDS, 2); }
    public void setPollSeconds(int seconds) { prefs.putInt(KEY_POLL_SECONDS, seconds); }

    // ── RPM scale ─────────────────────────────────────────────────────────────

    public int  getMaxRpm()                 { return prefs.getInt(KEY_MAX_RPM, 2000); }
    public void setMaxRpm(int maxRpm)       { prefs.putInt(KEY_MAX_RPM, maxRpm); }

    // ── Hidden sensors ────────────────────────────────────────────────────────
    // Stored as comma-separated sensor IDs, e.g. "hwmon0_temp1,hwmon2_fan1"

    public Set<String> getHiddenSensors() {
        String val = prefs.get(KEY_HIDDEN, "");
        if (val.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(val.split(",")));
    }

    public void setHiddenSensors(Set<String> ids) {
        prefs.put(KEY_HIDDEN, String.join(",", ids));
    }

    public boolean isHidden(String sensorId) {
        return getHiddenSensors().contains(sensorId);
    }
}
