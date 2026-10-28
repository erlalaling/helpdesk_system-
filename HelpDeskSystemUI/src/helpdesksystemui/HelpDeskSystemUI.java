package helpdesksystemui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class HelpDeskSystemUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default Swing Look and Feel
            }
            Ticketrepository repository = new Demoticketrepository();
            Mainframe frame = new Mainframe(repository);
            frame.setVisible(true);
        });
    }
}