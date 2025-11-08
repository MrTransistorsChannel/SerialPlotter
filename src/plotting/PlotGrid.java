package plotting;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PlotGrid {
    private final int nTicksX;
    private final int nTicksY;
    private double stepX, stepY;
    private double minX, maxX, minY, maxY;
    private double minTickX;
    private double minTickY;

    private final Color gridColor;
    private final Color axisColor;
    private final Stroke gridStroke = new BasicStroke(1.0f);
    private final Stroke axisStroke = new BasicStroke(1.0f);

    public PlotGrid(int nTicksX, int nTicksY, Color gridColor, Color axisColor) {
        this.nTicksX = nTicksX;
        this.nTicksY = nTicksY;
        this.gridColor = gridColor;
        this.axisColor = axisColor;
    }

    public void updateTicks(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        double xRange = maxX - minX;
        double yRange = maxY - minY;

        final double[] niceSteps = new double[]{1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0};

        double xExp = Math.floor(Math.log10(xRange / (nTicksX - 1)));
        double yExp = Math.floor(Math.log10(yRange / (nTicksY - 1)));
        double scaleX = Math.pow(10, xExp);
        double scaleY = Math.pow(10, yExp);

        double rawXTickStep = (xRange / (nTicksX - 1)) / scaleX;
        double rawYTickStep = (yRange / (nTicksY - 1)) / scaleY;
        stepX = 0;
        stepY = 0;
        for (double step : niceSteps) {
            if (rawXTickStep < step) {
                stepX = step * scaleX;
                break;
            }
        }
        for (double step : niceSteps) {
            if (rawYTickStep < step) {
                stepY = step * scaleY;
                break;
            }
        }
        minTickX = stepX * Math.floor(minX / stepX);
        minTickY = stepY * Math.floor(minY / stepY);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void paint(Graphics2D g, int xPos, int yPos, int width, int height) {
        g.setColor(gridColor);
        g.setStroke(gridStroke);
        g.drawRect(xPos, yPos, width, height);

        for (int i = 0; i < nTicksX; i++) {
            double x = minTickX + i * stepX;
            if (x < minX || x > maxX) continue;

            int lineXPos = (int) mapValue(x, minX, maxX, xPos, xPos + width);

            String tickLabel = String.valueOf(x);
            Rectangle2D fRect = g.getFontMetrics().getStringBounds(tickLabel, g);
            int xOffset = (int) fRect.getWidth() / 2;
            int yOffset = (int) (5 + fRect.getHeight());

            g.setColor(gridColor);
            g.setStroke(gridStroke);
            g.drawLine(lineXPos, yPos, lineXPos, yPos + height);

            g.setColor(axisColor);
            g.setStroke(axisStroke);
            g.drawString(tickLabel, lineXPos - xOffset, yPos + height + yOffset);
        }

        for (int i = 0; i < nTicksY; i++) {
            double y = minTickY + i * stepY;
            if (y < minY || y > maxY) continue;

            int lineYPos = (int) mapValue(y, minY, maxY, yPos + height, yPos);

            String tickLabel = String.valueOf(y);
            //String tickLabel = String.format("%.1E", y);
            Rectangle2D fRect = g.getFontMetrics().getStringBounds(tickLabel, g);
            int xOffset = (int) (5 + fRect.getWidth());
            int yOffset = (int) fRect.getHeight() / 2;

            g.setColor(gridColor);
            g.setStroke(gridStroke);
            g.drawLine(xPos, lineYPos, xPos + width, lineYPos);

            g.setColor(axisColor);
            g.setStroke(gridStroke);
            g.drawString(tickLabel, xPos - xOffset, lineYPos + yOffset);
        }

        g.setColor(axisColor);
        g.setStroke(axisStroke);

        int xAxisPos = (int) Math.clamp(mapValue(0, minY, maxY, yPos + height, yPos), yPos, yPos + height);
        int yAxisPos = (int) Math.clamp(mapValue(0, minX, maxX, xPos, xPos + width), xPos, xPos + width);

        g.drawLine(xPos, xAxisPos, xPos + width, xAxisPos);
        g.drawLine(yAxisPos, yPos, yAxisPos, yPos + height);
    }

    private double mapValue(double in, double inMin, double inMax, double outMin, double outMax) {
        return (in - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

}
