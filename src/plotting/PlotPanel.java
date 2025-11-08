package plotting;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PlotPanel extends JPanel {
    private final ArrayList<PlotLine> plotLines = new ArrayList<>();
    private final PlotGrid grid = new PlotGrid(5, 5, Color.lightGray, Color.black);
    private int[] plotMargins = {50, 50, 50, 50};

    public int lineCount() {
        return plotLines.size();
    }

    public PlotLine addLine(int bufferLength) {
        PlotLine newLine = new PlotLine(plotLines.size(), bufferLength);
        plotLines.add(newLine);
        return newLine;
    }

    public void removeLine(int id) {
        plotLines.remove(id);
    }

    public PlotLine getLine(int id) {
        return plotLines.get(id);
    }

    @Override
    public void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        super.paintComponent(g);
        setBackground(Color.white);

        if (plotLines.isEmpty())
            return;

        //TODO: allocate space for tick labels, legend and stuff, draw ticks and legend

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (PlotLine plotLine : plotLines) {
            if (plotLine.getMinX() < minX) minX = plotLine.getMinX();
            if (plotLine.getMaxX() > maxX) maxX = plotLine.getMaxX();
            if (plotLine.getMinY() < minY) minY = plotLine.getMinY();
            if (plotLine.getMaxY() > maxY) maxY = plotLine.getMaxY();
        }

        Rectangle bounds = g.getClipBounds();

        grid.updateTicks(minX, maxX, minY, maxY);
        grid.paint(g, plotMargins[0], plotMargins[2],
                bounds.width - plotMargins[1] - plotMargins[3],
                bounds.height - plotMargins[0] - plotMargins[2]);

        for (PlotLine line : plotLines) {
            line.paint(g, minX, maxX, minY, maxY, plotMargins[0], plotMargins[2],
                    bounds.width - plotMargins[1] - plotMargins[3],
                    bounds.height - plotMargins[0] - plotMargins[2]);
        }
    }

    public int[] getPlotMargins() {
        return plotMargins;
    }

    public void setPlotMargins(int[] plotMargins) {
        this.plotMargins = plotMargins;
    }
}
