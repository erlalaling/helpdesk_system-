package helpdesksystemui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Placeholderpanel extends JPanel {

    public Placeholderpanel(String pageTitle) {
        setLayout(new BorderLayout());
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel label = new JLabel(pageTitle + " screen", SwingConstants.CENTER);
        label.setFont(Uitheme.FONT_HEADER);
        label.setForeground(Uitheme.TEXT_SECONDARY);
        add(label, BorderLayout.CENTER);
    }
}