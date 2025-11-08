import jssc.*;
import plotting.PlotPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

public class MainWindow extends JFrame implements SerialPortEventListener {
    private JToggleButton connectButton;
    private JComboBox<String> portSelectCBox;
    private JComboBox<String> portSpeedCBox;
    private JPanel MainPanel;
    private JButton sendButton;
    private JTextField textSendField;
    private JComboBox<String> lineEndingSelectCBox;
    private PlotPanel serialPlotPanel;
    private StatusBar statusBar;
    private JButton portRefreshButton;

    private SerialPort serialPort;

    public MainWindow() {
        setContentPane(MainPanel);
        setTitle("Serial Plotter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        portRefreshButton.setBorder(new EmptyBorder(3, 3, 3, 3));
        serialPlotPanel.setPlotMargins(new int[]{50, 20, 20, 50});

        connectButton.addItemListener(event -> {
            // If serial port is not opened
            if (event.getStateChange() == ItemEvent.SELECTED) {
                serialPort = new SerialPort(Objects.requireNonNull(portSelectCBox.getSelectedItem()).toString());
                try {
                    serialPort.openPort();
                    serialPort.setEventsMask(SerialPort.MASK_RXCHAR + SerialPort.MASK_BREAK);
                    serialPort.addEventListener(this);
                    serialPort.setParams(Integer.parseInt(Objects.requireNonNull(portSpeedCBox.getSelectedItem()).toString()),
                            8, 1, 0);

                    connectButton.setText("Disconnect");
                    statusBar.setTimedStatus("Serial port opened successfully", 5000);

                    textSendField.setEnabled(true);
                    lineEndingSelectCBox.setEnabled(true);
                    sendButton.setEnabled(true);
                } catch (SerialPortException e) {
                    statusBar.setTimedStatus(String.format("Connection error: %s", e.getExceptionType()), 5000);
                    serialPort = null;
                    connectButton.setText("Connect");
                    connectButton.setSelected(false);
                }
            } else {
                try {
                    if (serialPort != null) {
                        serialPort.closePort();
                        serialPort = null;
                        statusBar.setTimedStatus("Serial port closed", 5000);
                    }
                    connectButton.setText("Connect");
                    textSendField.setEnabled(false);
                    lineEndingSelectCBox.setEnabled(false);
                    sendButton.setEnabled(false);
                } catch (SerialPortException e) {
                    statusBar.setTimedStatus(String.format("Error while disconnecting: %s", e.getExceptionType()), 5000);
                    serialPort = null;
                    connectButton.setText("Connect");
                    connectButton.setSelected(false);


                    textSendField.setEnabled(false);
                    lineEndingSelectCBox.setEnabled(false);
                    sendButton.setEnabled(false);
                }
            }
        });
        portRefreshButton.addActionListener(e -> {
            portSelectCBox.removeAllItems();
            if (SerialPortList.getPortNames().length > 0) {
                for (String portName : SerialPortList.getPortNames()) {
                    portSelectCBox.addItem(portName);
                }
                portSelectCBox.setEnabled(true);
                connectButton.setEnabled(true);
            } else {
                portSelectCBox.setEnabled(false);
                connectButton.setEnabled(false);
            }
        });

        // Add event listeners to send button and text field
        ActionListener sendButtonListener = event -> {
            String lineEnding = switch (lineEndingSelectCBox.getSelectedIndex()) {
                case 1 -> "\r";
                case 2 -> "\n";
                case 3 -> "\r\n";
                default -> "";
            };

            try {
                serialPort.writeString(textSendField.getText() + lineEnding);
            } catch (SerialPortException e) {
                statusBar.setTimedStatus(String.format("Serial port error: %s", e.getExceptionType()), 5000);
                serialPort = null;
                connectButton.setText("Connect");
                connectButton.setSelected(false);

                textSendField.setEnabled(false);
                lineEndingSelectCBox.setEnabled(false);
                sendButton.setEnabled(false);
            }
            textSendField.setText("");
        };
        sendButton.addActionListener(sendButtonListener);
        textSendField.addActionListener(sendButtonListener);

        if (SerialPortList.getPortNames().length > 0) {
            for (String portName : SerialPortList.getPortNames()) {
                portSelectCBox.addItem(portName);
            }
            portSelectCBox.setEnabled(true);
            connectButton.setEnabled(true);
        } else {
            portSelectCBox.setEnabled(false);
            connectButton.setEnabled(false);
        }
    }

    private final StringBuffer serialBuffer = new StringBuffer(256);
    private int lineCount = 0;

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {
            try {
                int byteCount = serialPort.getInputBufferBytesCount();
                if (byteCount == 0)
                    return;

                serialBuffer.append(new String(serialPort.readBytes(byteCount)));

                while (true) {
                    int EOLIndex = serialBuffer.indexOf("\n");
                    if (EOLIndex == -1)
                        break;
                    String line = serialBuffer.substring(0, EOLIndex).trim();
                    serialBuffer.delete(0, EOLIndex + 1);
                    if (line.isEmpty())
                        continue;

                    String[] fields = line.split("[, \t]+");
                    if (fields.length == 0)
                        continue;

                    //System.out.printf("Found %d fields\n", fields.length);

                    int validParts = 0;
                    for (String field : fields) {
                        Double value = null;

                        try {
                            value = Double.valueOf(field);
                        } catch (NumberFormatException ignored) {
                        }

                        if (value != null) {
                            if (validParts >= serialPlotPanel.lineCount()) {
                                System.out.println("Adding new line to graph\n");
                                serialPlotPanel.addLine(2000);
                            }
                            //System.out.printf("Adding new point (%f, %f) to line %d\n", (double) lineCount, value, validParts);
                            serialPlotPanel.getLine(validParts).addPoint(new Point2D.Double(lineCount, value));
                            validParts++;
                        }
                    }
                    while (validParts < serialPlotPanel.lineCount())
                        serialPlotPanel.removeLine(serialPlotPanel.lineCount() - 1);

                    lineCount++;
                }
                //System.out.printf("Graph bounds are [%f, %f, %f, %f]\n", serialPlotPanel.getMinX(), serialPlotPanel.getMaxX(), serialPlotPanel.getMinY(), serialPlotPanel.getMaxY());
                SwingUtilities.invokeLater(this::repaint);
            } catch (SerialPortException e) {
                statusBar.setTimedStatus(String.format("Serial port error: %s", e.getExceptionType()), 5000);
                serialPort = null;
                connectButton.setText("Connect");
                connectButton.setSelected(false);

                textSendField.setEnabled(false);
                lineEndingSelectCBox.setEnabled(false);
                sendButton.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        new MainWindow();

        /*plotting.PlotLine line = new plotting.PlotLine(0, 257);
        line.addPoint(new Point2D.Double(0, 0));

        for(int i = 0; i < 300; i++)
            line.addPoint(new Point2D.Double(100, 100));
        line.addPoint(new Point2D.Double(200, 200));*/
    }
}
