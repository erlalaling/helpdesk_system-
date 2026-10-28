package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Myticketspanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private final Ticketrepository repository;
    private final Consumer<Ticket> onViewTicket;

    private final JTextField searchField;
    private final JComboBox<String> statusFilterCombo;
    private final Tickettablemodel tableModel;
    private final JTable table;
    private final CardLayout listCardLayout;
    private final JPanel listContainer;
    private final JLabel countSummaryLabel;

    public Myticketspanel(Ticketrepository repository, Consumer<Ticket> onViewTicket) {
        this.repository = repository;
        this.onViewTicket = onViewTicket;

        setLayout(new BorderLayout(0, 16));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel mainCard = new JPanel(new BorderLayout(0, 14));
        mainCard.setBackground(Uitheme.CARD_BG);
        mainCard.setBorder(Uitheme.createCardBorder());

        // Header + Search/Filter Bar (Slide 3)
        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel("My Tickets");
        titleLabel.setFont(Uitheme.FONT_HEADER);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        countSummaryLabel = new JLabel();
        countSummaryLabel.setFont(Uitheme.FONT_SMALL);
        countSummaryLabel.setForeground(Uitheme.TEXT_SECONDARY);

        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(countSummaryLabel, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchIconLabel = new JLabel("Search:");
        searchIconLabel.setFont(Uitheme.FONT_BODY_BOLD);
        searchIconLabel.setForeground(Uitheme.TEXT_SECONDARY);

        searchField = new JTextField(22);
        searchField.setFont(Uitheme.FONT_BODY);
        searchField.setBorder(Uitheme.createInputBorder());
        searchField.setToolTipText("Search tickets by Ticket ID or Title...");

        statusFilterCombo = new JComboBox<>(new String[]{
            "Status: All",
            Ticketstatus.OPEN.getDisplayName(),
            Ticketstatus.IN_PROGRESS.getDisplayName(),
            Ticketstatus.RESOLVED.getDisplayName(),
            Ticketstatus.CLOSED.getDisplayName()
        });
        statusFilterCombo.setFont(Uitheme.FONT_BODY);

        JButton filterButton = new JButton("Filter");
        filterButton.setFont(Uitheme.FONT_BODY_BOLD);
        filterButton.setBackground(Uitheme.PRIMARY);
        filterButton.setForeground(Color.WHITE);
        filterButton.setFocusPainted(false);
        filterButton.setBorderPainted(false);
        filterButton.setOpaque(true);
        filterButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        filterButton.addActionListener(e -> applyFilters());

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(Uitheme.FONT_BODY);
        clearButton.setBackground(Color.WHITE);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
        statusFilterCombo.addActionListener(e -> applyFilters());

        filterBar.add(searchIconLabel);
        filterBar.add(searchField);
        filterBar.add(statusFilterCombo);
        filterBar.add(filterButton);
        filterBar.add(clearButton);

        topPanel.add(titleRow, BorderLayout.NORTH);
        topPanel.add(filterBar, BorderLayout.SOUTH);

        // Table + Empty State CardLayout
        tableModel = new Tickettablemodel(Tickettablemodel.Mode.MY_TICKETS);
        table = new JTable(tableModel);
        Recentticketpanel.configureTable(table, 3, 4, 5);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && (col == 5 || e.getClickCount() == 2)) {
                    Ticket selected = tableModel.getTicketAt(table.convertRowIndexToModel(row));
                    if (selected != null && Myticketspanel.this.onViewTicket != null) {
                        Myticketspanel.this.onViewTicket.accept(selected);
                    }
                }
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 5
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel emptyStatePanel = new JPanel(new BorderLayout());
        emptyStatePanel.setBackground(Color.WHITE);
        emptyStatePanel.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR));
        JLabel emptyLabel = new JLabel(
                "No tickets match your current search or status filter.",
                SwingConstants.CENTER
        );
        emptyLabel.setFont(Uitheme.FONT_BODY);
        emptyLabel.setForeground(Uitheme.TEXT_SECONDARY);
        emptyStatePanel.add(emptyLabel, BorderLayout.CENTER);

        listCardLayout = new CardLayout();
        listContainer = new JPanel(listCardLayout);
        listContainer.add(scrollPane, CARD_TABLE);
        listContainer.add(emptyStatePanel, CARD_EMPTY);

        mainCard.add(topPanel, BorderLayout.NORTH);
        mainCard.add(listContainer, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
        refreshData();
    }

    public final void refreshData() {
        applyFilters();
    }

    private void applyFilters() {
        if (repository == null) {
            return;
        }
        // Requester scope: explicitly scoped to DEMO_CURRENT_REQUESTER ("Sean Gerolaga")
        List<Ticket> myTickets = repository.findByRequester(Demoticketrepository.DEMO_CURRENT_REQUESTER);
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        int statusIndex = statusFilterCombo.getSelectedIndex();
        String selectedStatus = statusFilterCombo.getSelectedItem() == null
                ? ""
                : statusFilterCombo.getSelectedItem().toString();

        List<Ticket> filtered = myTickets.stream()
                .filter(t -> {
                    boolean matchesQuery = query.isEmpty()
                            || t.getId().toLowerCase(Locale.ROOT).contains(query)
                            || t.getTitle().toLowerCase(Locale.ROOT).contains(query)
                            || t.getCategory().toLowerCase(Locale.ROOT).contains(query);
                    boolean matchesStatus = (statusIndex <= 0)
                            || t.getStatus().getDisplayName().equalsIgnoreCase(selectedStatus);
                    return matchesQuery && matchesStatus;
                })
                .collect(Collectors.toList());

        tableModel.setTickets(filtered);
        countSummaryLabel.setText(
                "Showing " + filtered.size() + " of " + myTickets.size()
                + " tickets for " + Demoticketrepository.DEMO_CURRENT_REQUESTER
        );
        listCardLayout.show(listContainer, filtered.isEmpty() ? CARD_EMPTY : CARD_TABLE);
    }
}