package se.viktor.fancontrollerlinux.service;

import se.viktor.fancontrollerlinux.model.FanSensor;
import se.viktor.fancontrollerlinux.model.TempSensor;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes fan/temperature data via the Linux hwmon sysfs interface.
 *
 *   /sys/class/hwmon/hwmon{N}/
 *     name          → chip driver  (e.g. "nct6775", "k10temp", "amdgpu")
 *     temp{N}_input → millidegrees C
 *     temp{N}_label → label (optional)
 *     fan{N}_input  → RPM
 *     fan{N}_label  → label (optional)
 *     pwm{N}        → duty cycle 0–255
 *     pwm{N}_enable → 0=full, 1=manual, 2=auto
 */
public class HwmonService {

    private static final Path BASE = Path.of("/sys/class/hwmon");
    private static final int  MAX  = 12;

    public List<TempSensor> readTemps() {
        var list = new ArrayList<TempSensor>();
        for (Path hw : hwmons()) {
            String driver = read(hw.resolve("name")).trim();
            for (int i = 1; i <= MAX; i++) {
                Path p = hw.resolve("temp" + i + "_input");
                if (!Files.exists(p)) continue;
                String raw = read(p).trim();
                if (raw.isEmpty()) continue;
                double c     = Long.parseLong(raw) / 1000.0;
                String label = readOr(hw.resolve("temp" + i + "_label"), "Temp " + i);
                list.add(new TempSensor(hw.getFileName() + "_temp" + i, label, driver, c));
            }
        }
        return list;
    }

    public List<FanSensor> readFans() {
        var list = new ArrayList<FanSensor>();
        for (Path hw : hwmons()) {
            String driver = read(hw.resolve("name")).trim();
            for (int i = 1; i <= MAX; i++) {
                Path p = hw.resolve("fan" + i + "_input");
                if (!Files.exists(p)) continue;
                String raw = read(p).trim();
                if (raw.isEmpty()) continue;
                int    rpm     = Integer.parseInt(raw);
                String label   = readOr(hw.resolve("fan" + i + "_label"), "Fan " + i);
                Path   pwmPath = hw.resolve("pwm" + i);
                int    pwm     = 0;
                boolean writable = false;
                if (Files.exists(pwmPath)) {
                    String pr = read(pwmPath).trim();
                    if (!pr.isEmpty()) pwm = Integer.parseInt(pr);
                    writable = Files.isWritable(pwmPath);
                }
                list.add(FanSensor.of(hw.getFileName() + "_fan" + i, label, driver, rpm, pwm, writable));
            }
        }
        return list;
    }

    /**
     * @param hwmonName  e.g. "hwmon2"
     * @param pwmIndex   e.g. 1 (for pwm1)
     * @param percent    0–100
     */
    public void setPwmPercent(String hwmonName, int pwmIndex, int percent) {
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException("Percent must be 0–100");
        Path hw  = BASE.resolve(hwmonName);
        Path en  = hw.resolve("pwm" + pwmIndex + "_enable");
        Path pwm = hw.resolve("pwm" + pwmIndex);
        try {
            if (Files.exists(en)) Files.writeString(en, "1");
            Files.writeString(pwm, String.valueOf(Math.round(percent / 100.0 * 255)));
        } catch (IOException e) {
            throw new RuntimeException(
                "Kan inte skriva till " + pwm + " — kör som root eller sätt rätt permissions.", e);
        }
    }

    private List<Path> hwmons() {
        var out = new ArrayList<Path>();
        if (!Files.exists(BASE)) return out;
        try (var ds = Files.newDirectoryStream(BASE, "hwmon*")) {
            ds.forEach(out::add);
        } catch (IOException ignored) {}
        out.sort(null);
        return out;
    }

    private String read(Path p) {
        try { return Files.readString(p); } catch (IOException e) { return ""; }
    }

    private String readOr(Path p, String fallback) {
        String s = read(p).trim();
        return s.isEmpty() ? fallback : s;
    }
}
