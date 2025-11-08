import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraphPanel extends JPanel {
    private final ArrayList<GraphLine> graphLines = new ArrayList<>();

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

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
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
