package se.viktor.fancontrollerlinux.model;

public record FanSensor(
        String id,
        String label,
        String driver,
        int rpm,
        int pwm,
        int pwmPercent,
        boolean pwmWritable
) {
    public static FanSensor of(String id, String label, String driver,
                                int rpm, int pwm, boolean writable) {
        int pct = (int) Math.round((pwm / 255.0) * 100);
        return new FanSensor(id, label, driver, rpm, pwm, pct, writable);
    }
}
