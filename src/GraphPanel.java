import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraphPanel extends JPanel {
    private final ArrayList<GraphLine> graphLines = new ArrayList<>();
    private double minX = 0, maxX = 0, minY = 0, maxY = 0;

    public int lineCount() {
        return graphLines.size();
    }

    public GraphLine addLine(int bufferLength) {
        GraphLine newLine = new GraphLine(graphLines.size(), bufferLength);
        graphLines.add(newLine);
        return newLine;
    }

    public void removeLine(int id) {
        graphLines.remove(id);
    }

    public GraphLine getLine(int id) {
        return graphLines.get(id);
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

    @Override
    public void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        super.paintComponent(g);
        setBackground(Color.white);

        if (graphLines.isEmpty())
            return;

        //TODO: allocate space for tick labels, legend and stuff, draw ticks and legend

        minX = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
        for (GraphLine graphLine : graphLines) {
            if (graphLine.getMinX() < minX) minX = graphLine.getMinX();
            if (graphLine.getMaxX() > maxX) maxX = graphLine.getMaxX();
            if (graphLine.getMinY() < minY) minY = graphLine.getMinY();
            if (graphLine.getMaxY() > maxY) maxY = graphLine.getMaxY();
        }

        for (GraphLine line : graphLines) {
            Rectangle bounds = g.getClipBounds();
            line.paint(g, minX, maxX, minY, maxY, 50, 50, bounds.width - 100, bounds.height - 100);
        }
    }
}
