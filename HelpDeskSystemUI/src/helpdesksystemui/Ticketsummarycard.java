package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Ticketsummarycard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;

    public Ticketsummarycard(String title, int initialValue, Color accentColor) {
        setLayout(new BorderLayout(0, 8));
        setBackground(Uitheme.CARD_BG);
        setPreferredSize(new Dimension(165, 92));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, accentColor == null ? Uitheme.PRIMARY : accentColor),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        titleLabel = new JLabel(title);
        titleLabel.setFont(Uitheme.FONT_CARD_TITLE);
        titleLabel.setForeground(Uitheme.TEXT_SECONDARY);

        valueLabel = new JLabel(String.valueOf(initialValue));
        valueLabel.setFont(Uitheme.FONT_CARD_VALUE);
        valueLabel.setForeground(Uitheme.TEXT_PRIMARY);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(int value) {
        valueLabel.setText(String.valueOf(value));
    }

    public void setValueText(String text) {
        valueLabel.setText(text == null ? "0" : text);
    }
}