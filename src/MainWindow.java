import jssc.*;
import plotting.PlotPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

public class MainWindow extends JFrame implements SerialPortEventListener {
    private JToggleButton connectButton;
    private JComboBox<String> portSelectCBox;
    private JComboBox<String> portSpeedCBox;
    private JPanel MainPanel;
    private PlotPanel serialPlotPanel;
    private StatusBar statusBar;
    private JButton portRefreshButton;
    private JTextArea terminal;

    private SerialPort serialPort;
    private boolean isPlotterLine = false;

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
                    serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);  // Clear buffers
                    serialPort.setEventsMask(SerialPort.MASK_RXCHAR + SerialPort.MASK_BREAK);
                    serialPort.addEventListener(this);
                    serialPort.setParams(Integer.parseInt(Objects.requireNonNull(portSpeedCBox.getSelectedItem()).toString()),
                            8, 1, 0);

                    connectButton.setText("Disconnect");
                    statusBar.setTimedStatus("Serial port opened successfully", 5000);

                    // Enable terminal panel
                    terminal.setEnabled(true);
                    terminal.setText("");
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
                } catch (SerialPortException e) {
                    statusBar.setTimedStatus(String.format("Error while disconnecting: %s", e.getExceptionType()), 5000);
                    serialPort = null;
                    connectButton.setText("Connect");
                    connectButton.setSelected(false);
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

        // Add terminal panel key press handling
        terminal.setFocusTraversalKeysEnabled(false);
        ((DefaultCaret) terminal.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        // Add copy contextual menu to the text input field.
        JPopupMenu menu = new JPopupMenu();
        Action copy = new DefaultEditorKit.CopyAction();
        copy.putValue(Action.NAME, "Copy");
        menu.add(copy);

        terminal.setComponentPopupMenu(menu);


        terminal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_TAB:
                    case KeyEvent.VK_BACK_SPACE:
                    case KeyEvent.VK_DELETE:
                        e.consume();
                }
            }

            @Override
            public void keyTyped(KeyEvent event) {
                event.consume();
                int keyCode = event.getKeyChar();
                if (keyCode > 127) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                try {
                    if (serialPort != null)
                        serialPort.writeByte((byte) keyCode);
                } catch (SerialPortException e) {
                    statusBar.setTimedStatus(String.format("Serial port error: %s", e.getExceptionType()), 5000);
                    serialPort = null;
                    connectButton.setText("Connect");
                    connectButton.setSelected(false);
                }
            }
        });

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

    private final StringBuffer plotterBuffer = new StringBuffer(256);
    private int lineCount = 0;

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {
            try {
                int byteCount = serialPort.getInputBufferBytesCount();
                if (byteCount == 0)
                    return;

                byte[] data = serialPort.readBytes(byteCount);

                for (byte chr : data) {
                    switch (chr) {
                        case '\b':  // Received a backspace, remove last character
                            terminal.getDocument().remove(terminal.getText().length() - 1, 1);
                            break;
                        case '\n':  // Received a newline, disable plotting if enabled
                            // If plotting enabled, send to plotter parser, else send to the terminal
                            if (isPlotterLine)
                                plotterBuffer.append((char) chr);
                            else {
                                terminal.append(String.valueOf((char) chr));
                                // Move cursor to the end
                                terminal.setCaretPosition(terminal.getDocument().getLength());
                                // Remove old lines to prevent infinite memory allocation
                                while (terminal.getText().length() - 100000 > 0) {
                                    terminal.getDocument().remove(0, terminal.getText().indexOf('\n') + 1);
                                }
                            }

                            isPlotterLine = false;
                            break;
                        case '\1':  // received a StartOfHeader symbol, enable plotting
                            isPlotterLine = true;
                            break;
                        default:    // Any other
                            if (isPlotterLine)
                                plotterBuffer.append((char) chr);
                            else {
                                terminal.append(String.valueOf((char) chr));
                                // Move cursor to the end
                                terminal.setCaretPosition(terminal.getDocument().getLength());
                                // Remove old lines to prevent infinite memory allocation
                                while (terminal.getText().length() - 100000 > 0) {
                                    terminal.getDocument().remove(0, terminal.getText().indexOf('\n') + 1);
                                }
                            }
                    }
                }

                while (true) {
                    int EOLIndex = plotterBuffer.indexOf("\n");
                    if (EOLIndex == -1)
                        break;
                    String line = plotterBuffer.substring(0, EOLIndex).trim();
                    plotterBuffer.delete(0, EOLIndex + 1);
                    if (line.isEmpty())
                        continue;

                    String[] fields = line.split("[, \t]+");
                    if (fields.length == 0)
                        continue;

                    int validParts = 0;
                    for (String field : fields) {
                        Double value = null;

                        try {
                            value = Double.valueOf(field);
                        } catch (NumberFormatException ignored) {
                        }

                        if (value != null) {
                            if (validParts >= serialPlotPanel.lineCount()) {
                                serialPlotPanel.addLine(2000);
                            }
                            serialPlotPanel.getLine(validParts).addPoint(new Point2D.Double(lineCount, value));
                            validParts++;
                        }
                    }
                    while (validParts < serialPlotPanel.lineCount())
                        serialPlotPanel.removeLine(serialPlotPanel.lineCount() - 1);

                    lineCount++;
                }
                SwingUtilities.invokeLater(this::repaint);
            } catch (SerialPortException e) {
                statusBar.setTimedStatus(String.format("Serial port error: %s", e.getExceptionType()), 5000);
                serialPort = null;
                connectButton.setText("Connect");
                connectButton.setSelected(false);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
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
