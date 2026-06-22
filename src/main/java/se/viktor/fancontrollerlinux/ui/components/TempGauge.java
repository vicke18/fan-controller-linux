package se.viktor.fancontroller.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se.viktor.fancontroller.ui.util.ColorScale;

/**
 * Canvas-based circular gauge for one temperature sensor.
 * Draws a clockwise arc from 12 o'clock proportional to temp/100.
 */
public class TempGauge extends Canvas {

    private static final double SIZE   = 110;
    private static final double CX     = SIZE / 2;
    private static final double CY     = SIZE / 2;
    private static final double RADIUS = 42;
    private static final double STROKE = 7;

    public TempGauge() {
        super(SIZE, SIZE);
    }

    public void update(double tempCelsius) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);

        // Background ring
        gc.setStroke(Color.rgb(255, 255, 255, 0.07));
        gc.setLineWidth(STROKE);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeArc(CX - RADIUS, CY - RADIUS, RADIUS * 2, RADIUS * 2,
                     0, 360, ArcType.OPEN);

        // Colored arc — clockwise from top (90°), negative extent = clockwise
        Color arcColor = ColorScale.forTemp(tempCelsius);
        double fraction = Math.min(tempCelsius / 100.0, 1.0);
        gc.setStroke(arcColor);
        gc.setEffect(new DropShadow(10, arcColor));
        gc.strokeArc(CX - RADIUS, CY - RADIUS, RADIUS * 2, RADIUS * 2,
                     90, -(fraction * 360), ArcType.OPEN);
        gc.setEffect(null);

        // Temperature value
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.rgb(255, 255, 255, 0.92));
        gc.setFont(Font.font("DejaVu Sans Mono", FontWeight.BOLD, 15));
        gc.fillText(Math.round(tempCelsius) + "°", CX, CY + 5);

        // Unit label
        gc.setFill(Color.rgb(255, 255, 255, 0.28));
        gc.setFont(Font.font("DejaVu Sans Mono", 8));
        gc.fillText("CELSIUS", CX, CY + 18);
    }
}
