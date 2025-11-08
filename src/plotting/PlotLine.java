package plotting;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PlotLine {
    private String label;
    private final Color color;
    private final Stroke stroke = new BasicStroke(1.0f);
    private final int bufferSize;

    private final ArrayList<Point2D.Double> points = new ArrayList<>();

    // Fast min-max calculation variables
    private final ArrayList<Integer> minXDeque = new ArrayList<>();
    private final ArrayList<Integer> maxXDeque = new ArrayList<>();
    private final ArrayList<Integer> minYDeque = new ArrayList<>();
    private final ArrayList<Integer> maxYDeque = new ArrayList<>();

    private double minX, maxX, minY, maxY;

    public PlotLine(int id, int bufferLength) {
        this.bufferSize = bufferLength;

        Color[] graphColors = {
                Color.decode("#0000FF"),
                Color.decode("#FF0000"),
                Color.decode("#009900"),
                Color.decode("#FF9900"),
                Color.decode("#CC00CC"),
                Color.decode("#666666"),
                Color.decode("#00CCFF"),
                Color.decode("#000000"),
        };
        this.color = graphColors[id % graphColors.length];
    }

    public void addPoint(Point2D.Double point) {
        points.add(point);

        if (points.size() > bufferSize) {
            points.removeFirst();
            minXDeque.replaceAll(integer -> integer - 1);
            maxXDeque.replaceAll(integer -> integer - 1);
            minYDeque.replaceAll(integer -> integer - 1);
            maxYDeque.replaceAll(integer -> integer - 1);
        }

        if (!minXDeque.isEmpty() && minXDeque.getFirst() < 0)
            minXDeque.removeFirst();
        if (!maxXDeque.isEmpty() && maxXDeque.getFirst() < 0)
            maxXDeque.removeFirst();
        if (!minYDeque.isEmpty() && minYDeque.getFirst() < 0)
            minYDeque.removeFirst();
        if (!maxYDeque.isEmpty() && maxYDeque.getFirst() < 0)
            maxYDeque.removeFirst();

        while (!minXDeque.isEmpty() && points.get(minXDeque.getLast()).x >= point.x)
            minXDeque.removeLast();
        while (!maxXDeque.isEmpty() && points.get(maxXDeque.getLast()).x <= point.x)
            maxXDeque.removeLast();
        while (!minYDeque.isEmpty() && points.get(minYDeque.getLast()).y >= point.y)
            minYDeque.removeLast();
        while (!maxYDeque.isEmpty() && points.get(maxYDeque.getLast()).y <= point.y)
            maxYDeque.removeLast();

        minXDeque.addLast(points.size() - 1);
        maxXDeque.addLast(points.size() - 1);
        minYDeque.addLast(points.size() - 1);
        maxYDeque.addLast(points.size() - 1);

        minX = points.get(minXDeque.getFirst()).x;
        maxX = points.get(maxXDeque.getFirst()).x;
        minY = points.get(minYDeque.getFirst()).y;
        maxY = points.get(maxYDeque.getFirst()).y;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void paint(Graphics2D g, double minX, double maxX, double minY, double maxY, int xPos, int yPos, int width, int height) {
        if (points.isEmpty())
            return;

        g.setColor(color);
        g.setStroke(stroke);

        for (int i = 0; i < points.size() - 1; i++) {
            g.drawLine(
                    (int) mapValue(points.get(i).getX(), minX, maxX, xPos, xPos + width),
                    (int) mapValue(points.get(i).getY(), minY, maxY, yPos + height, yPos),
                    (int) mapValue(points.get(i + 1).getX(), minX, maxX, xPos, xPos + width),
                    (int) mapValue(points.get(i + 1).getY(), minY, maxY, yPos + height, yPos)
            );
        }
    }

    private double mapValue(double in, double inMin, double inMax, double outMin, double outMax) {
        return (in - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}