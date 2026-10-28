package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Sidebarpanel extends JPanel {

    private final Map<Page, NavButton> navButtons = new EnumMap<>(Page.class);
    private Consumer<Page> navigationListener;
    private Page activePage = Page.DASHBOARD;

    public Sidebarpanel(Consumer<Page> navigationListener) {
        this.navigationListener = navigationListener;

        setLayout(new BorderLayout());
        setBackground(Uitheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(235, 0));
        setBorder(BorderFactory.createEmptyBorder(22, 16, 22, 16));

        // Brand Header (SARMI HELP DESK / Ticket Management)
        JPanel brandPanel = new JPanel(new BorderLayout(0, 4));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 24, 8));

        JLabel brandTitle = new JLabel("SARMI HELP DESK");
        brandTitle.setFont(Uitheme.FONT_BRAND);
        brandTitle.setForeground(Uitheme.SIDEBAR_TEXT);

        JLabel brandSubtitle = new JLabel("Ticket Management");
        brandSubtitle.setFont(Uitheme.FONT_SMALL);
        brandSubtitle.setForeground(Uitheme.SIDEBAR_MUTED);

        brandPanel.add(brandTitle, BorderLayout.NORTH);
        brandPanel.add(brandSubtitle, BorderLayout.SOUTH);

        // Navigation Items
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        menuPanel.setOpaque(false);

        for (Page page : Page.values()) {
            if (!page.isSidebarVisible()) {
                continue;
            }
            NavButton button = new NavButton(page);
            button.addActionListener(e -> {
                if (this.navigationListener != null) {
                    this.navigationListener.accept(page);
                }
            });
            navButtons.put(page, button);
            menuPanel.add(button);
        }

        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setOpaque(false);
        menuWrapper.add(menuPanel, BorderLayout.NORTH);

        add(brandPanel, BorderLayout.NORTH);
        add(menuWrapper, BorderLayout.CENTER);

        setActivePage(Page.DASHBOARD);
    }

    public void setNavigationListener(Consumer<Page> navigationListener) {
        this.navigationListener = navigationListener;
    }

    public void setActivePage(Page page) {
        if (page != null && page.isSidebarVisible()) {
            this.activePage = page;
        }
        for (Map.Entry<Page, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey() == this.activePage);
        }
    }

    private static class NavButton extends JButton {

        private final Page page;
        private boolean active;
        private boolean hovered;

        NavButton(Page page) {
            this.page = page;
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    updateStyle();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    updateStyle();
                }
            });

            updateStyle();
        }

        void setActive(boolean active) {
            this.active = active;
            updateStyle();
        }

        private void updateStyle() {
            String indicator = active ? "●  " : "○  ";
            setText(indicator + page.getLabel());
            setFont(active ? Uitheme.FONT_BODY_BOLD : Uitheme.FONT_BODY);
            setForeground(active ? Color.WHITE : Uitheme.SIDEBAR_TEXT);
            if (active) {
                setBackground(Uitheme.SIDEBAR_ACTIVE);
            } else if (hovered) {
                setBackground(Uitheme.SIDEBAR_HOVER);
            } else {
                setBackground(Uitheme.SIDEBAR_BG);
            }
        }
    }
}