package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class Recentticketpanel extends JPanel {

    private final Tickettablemodel tableModel;
    private final JTable table;
    private Consumer<Ticket> onViewTicket;

    public Recentticketpanel() {
        this(null);
    }

    public Recentticketpanel(Consumer<Ticket> onViewTicket) {
        this.onViewTicket = onViewTicket;
        this.tableModel = new Tickettablemodel(Tickettablemodel.Mode.DASHBOARD);
        this.table = new JTable(tableModel);

        setLayout(new BorderLayout(0, 12));
        setBackground(Uitheme.CARD_BG);
        setBorder(Uitheme.createCardBorder());

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("RECENT TICKETS");
        titleLabel.setFont(Uitheme.FONT_SECTION);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Latest requests submitted to the Help Desk");
        subtitleLabel.setFont(Uitheme.FONT_SMALL);
        subtitleLabel.setForeground(Uitheme.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        configureTable(table, 2, 3, 4);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && (col == 4 || e.getClickCount() == 2)) {
                    Ticket selected = tableModel.getTicketAt(table.convertRowIndexToModel(row));
                    if (selected != null && Recentticketpanel.this.onViewTicket != null) {
                        Recentticketpanel.this.onViewTicket.accept(selected);
                    }
                }
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 4
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setOnViewTicket(Consumer<Ticket> onViewTicket) {
        this.onViewTicket = onViewTicket;
    }

    public void setTickets(List<Ticket> tickets) {
        tableModel.setTickets(tickets);
    }

    public static void configureTable(JTable table, int priorityCol, int statusCol, int actionCol) {
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setGridColor(Uitheme.BORDER_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(Uitheme.FONT_BODY);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(Uitheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setReorderingAllowed(false);

        TextRenderer textRenderer = new TextRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
        }
        if (priorityCol >= 0 && priorityCol < table.getColumnCount()) {
            table.getColumnModel().getColumn(priorityCol).setCellRenderer(new PriorityRenderer());
        }
        if (statusCol >= 0 && statusCol < table.getColumnCount()) {
            table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusRenderer());
        }
        if (actionCol >= 0 && actionCol < table.getColumnCount()) {
            table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionButtonRenderer());
            table.getColumnModel().getColumn(actionCol).setMaxWidth(100);
            table.getColumnModel().getColumn(actionCol).setPreferredWidth(90);
        }
    }

    public static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setBackground(new Color(248, 250, 252));
            label.setForeground(Uitheme.TEXT_SECONDARY);
            label.setFont(Uitheme.FONT_CARD_TITLE);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Uitheme.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            return label;
        }
    }

    public static class TextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setFont(column == 0 ? Uitheme.FONT_BODY_BOLD : Uitheme.FONT_BODY);
            label.setForeground(Uitheme.TEXT_PRIMARY);
            label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return label;
        }
    }

    public static class BadgeRenderer extends DefaultTableCellRenderer {
        protected JPanel createBadgePanel(
                JTable table, String text, Color bg, Color fg, boolean isSelected) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(text, SwingConstants.CENTER);
            badge.setOpaque(true);
            badge.setBackground(bg);
            badge.setForeground(fg);
            badge.setFont(Uitheme.FONT_BADGE);
            badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            wrapper.add(badge);
            return wrapper;
        }
    }

    public static class PriorityRenderer extends BadgeRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Ticketpriority priority = value instanceof Ticketpriority
                    ? (Ticketpriority) value
                    : Ticketpriority.MEDIUM;
            Color bg;
            Color fg;
            switch (priority) {
                case URGENT:
                case HIGH:
                    bg = Uitheme.BADGE_HIGH_BG;
                    fg = Uitheme.BADGE_HIGH_FG;
                    break;
                case LOW:
                    bg = Uitheme.BADGE_LOW_BG;
                    fg = Uitheme.BADGE_LOW_FG;
                    break;
                case MEDIUM:
                default:
                    bg = Uitheme.BADGE_MEDIUM_BG;
                    fg = Uitheme.BADGE_MEDIUM_FG;
                    break;
            }
            return createBadgePanel(table, priority.getDisplayName(), bg, fg, isSelected);
        }
    }

    public static class StatusRenderer extends BadgeRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Ticketstatus status = value instanceof Ticketstatus
                    ? (Ticketstatus) value
                    : Ticketstatus.OPEN;
            Color bg;
            Color fg;
            switch (status) {
                case IN_PROGRESS:
                    bg = Uitheme.BADGE_PROGRESS_BG;
                    fg = Uitheme.BADGE_PROGRESS_FG;
                    break;
                case RESOLVED:
                    bg = Uitheme.BADGE_RESOLVED_BG;
                    fg = Uitheme.BADGE_RESOLVED_FG;
                    break;
                case CLOSED:
                    bg = Uitheme.BADGE_CLOSED_BG;
                    fg = Uitheme.BADGE_CLOSED_FG;
                    break;
                case OPEN:
                default:
                    bg = Uitheme.BADGE_OPEN_BG;
                    fg = Uitheme.BADGE_OPEN_FG;
                    break;
            }
            return createBadgePanel(table, status.getDisplayName(), bg, fg, isSelected);
        }
    }

    public static class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JButton btn = new JButton(value == null ? "View" : value.toString());
            btn.setFont(Uitheme.FONT_SMALL);
            btn.setForeground(Color.WHITE);
            btn.setBackground(Uitheme.PRIMARY);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
            panel.add(btn);
            return panel;
        }
    }
}