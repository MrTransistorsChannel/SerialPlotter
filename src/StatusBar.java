import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel statusLabel;
    private Timer messageTimer;

    public StatusBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        statusLabel = new JLabel(" ");
        Component horizontalSpacer = Box.createHorizontalGlue();

        add(statusLabel);
        add(horizontalSpacer);

        setBorder(BorderFactory.createBevelBorder(1));
        setPreferredSize(new Dimension(-1, 20));
    }

    public void setStatus(String message) {
        stopTimer();
        statusLabel.setText(message);
    }

    public void setTimedStatus(String message, int milliseconds) {
        setStatus(message);
        startTimer(milliseconds);
    }

    public void clearStatus() {
        setStatus(" ");
    }

    private void startTimer(int milliseconds) {
        stopTimer();
        messageTimer = new Timer(milliseconds, e -> clearStatus());
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    private void stopTimer() {
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }
    }
}