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

public class Allticketspanel extends JPanel {

    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    private final Ticketrepository repository;
    private final Consumer<Ticket> onViewTicket;

    private final JTextField searchField;
    private final JComboBox<String> statusCombo;
    private final JComboBox<String> priorityCombo;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> assigneeCombo;
    private final Tickettablemodel tableModel;
    private final JTable table;
    private final CardLayout listCardLayout;
    private final JPanel listContainer;
    private final JLabel countSummaryLabel;

    public Allticketspanel(Ticketrepository repository, Consumer<Ticket> onViewTicket) {
        this.repository = repository;
        this.onViewTicket = onViewTicket;

        setLayout(new BorderLayout(0, 16));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel mainCard = new JPanel(new BorderLayout(0, 14));
        mainCard.setBackground(Uitheme.CARD_BG);
        mainCard.setBorder(Uitheme.createCardBorder());

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel("All Tickets");
        titleLabel.setFont(Uitheme.FONT_HEADER);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        countSummaryLabel = new JLabel();
        countSummaryLabel.setFont(Uitheme.FONT_SMALL);
        countSummaryLabel.setForeground(Uitheme.TEXT_SECONDARY);

        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(countSummaryLabel, BorderLayout.EAST);

        // Multi-criteria Filter Bar (Slide 4: search, status, priority, category, assignee)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setFont(Uitheme.FONT_BODY);
        searchField.setBorder(Uitheme.createInputBorder());
        searchField.setToolTipText("Search by Ticket ID, Requester, or Title...");

        statusCombo = new JComboBox<>(new String[]{
            "Status: All",
            Ticketstatus.OPEN.getDisplayName(),
            Ticketstatus.IN_PROGRESS.getDisplayName(),
            Ticketstatus.RESOLVED.getDisplayName(),
            Ticketstatus.CLOSED.getDisplayName()
        });
        statusCombo.setFont(Uitheme.FONT_SMALL);

        priorityCombo = new JComboBox<>(new String[]{
            "Priority: All",
            Ticketpriority.LOW.getDisplayName(),
            Ticketpriority.MEDIUM.getDisplayName(),
            Ticketpriority.HIGH.getDisplayName(),
            Ticketpriority.URGENT.getDisplayName()
        });
        priorityCombo.setFont(Uitheme.FONT_SMALL);

        String[] categories = new String[Demoticketrepository.CATEGORIES.length + 1];
        categories[0] = "Category: All";
        System.arraycopy(Demoticketrepository.CATEGORIES, 0, categories, 1, Demoticketrepository.CATEGORIES.length);
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(Uitheme.FONT_SMALL);

        String[] assignees = new String[Demoticketrepository.TECHNICIANS.length + 1];
        assignees[0] = "Assignee: All";
        System.arraycopy(Demoticketrepository.TECHNICIANS, 0, assignees, 1, Demoticketrepository.TECHNICIANS.length);
        assigneeCombo = new JComboBox<>(assignees);
        assigneeCombo.setFont(Uitheme.FONT_SMALL);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(Uitheme.FONT_BODY_BOLD);
        filterBtn.setBackground(Uitheme.PRIMARY);
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setFocusPainted(false);
        filterBtn.setBorderPainted(false);
        filterBtn.setOpaque(true);
        filterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = new JButton("Clear");
        resetBtn.setFont(Uitheme.FONT_SMALL);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusCombo.setSelectedIndex(0);
            priorityCombo.setSelectedIndex(0);
            categoryCombo.setSelectedIndex(0);
            assigneeCombo.setSelectedIndex(0);
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
        statusCombo.addActionListener(e -> applyFilters());
        priorityCombo.addActionListener(e -> applyFilters());
        categoryCombo.addActionListener(e -> applyFilters());
        assigneeCombo.addActionListener(e -> applyFilters());

        filterBar.add(new JLabel("Search:"));
        filterBar.add(searchField);
        filterBar.add(statusCombo);
        filterBar.add(priorityCombo);
        filterBar.add(categoryCombo);
        filterBar.add(assigneeCombo);
        filterBar.add(filterBtn);
        filterBar.add(resetBtn);

        topPanel.add(titleRow, BorderLayout.NORTH);
        topPanel.add(filterBar, BorderLayout.SOUTH);

        // Table + Empty state
        tableModel = new Tickettablemodel(Tickettablemodel.Mode.ALL_TICKETS);
        table = new JTable(tableModel);
        Recentticketpanel.configureTable(table, 3, 4, 5);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && (col == 5 || e.getClickCount() == 2)) {
                    Ticket selected = tableModel.getTicketAt(table.convertRowIndexToModel(row));
                    if (selected != null && Allticketspanel.this.onViewTicket != null) {
                        Allticketspanel.this.onViewTicket.accept(selected);
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
                "No tickets in the organization queue match the selected filters.",
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
        List<Ticket> allTickets = repository.findAll();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        int statusIdx = statusCombo.getSelectedIndex();
        String statusVal = statusCombo.getSelectedItem() == null ? "" : statusCombo.getSelectedItem().toString();

        int priorityIdx = priorityCombo.getSelectedIndex();
        String priorityVal = priorityCombo.getSelectedItem() == null ? "" : priorityCombo.getSelectedItem().toString();

        int categoryIdx = categoryCombo.getSelectedIndex();
        String categoryVal = categoryCombo.getSelectedItem() == null ? "" : categoryCombo.getSelectedItem().toString();

        int assigneeIdx = assigneeCombo.getSelectedIndex();
        String assigneeVal = assigneeCombo.getSelectedItem() == null ? "" : assigneeCombo.getSelectedItem().toString();

        List<Ticket> filtered = allTickets.stream()
                .filter(t -> {
                    boolean matchesQuery = query.isEmpty()
                            || t.getId().toLowerCase(Locale.ROOT).contains(query)
                            || t.getTitle().toLowerCase(Locale.ROOT).contains(query)
                            || t.getRequester().toLowerCase(Locale.ROOT).contains(query);
                    boolean matchesStatus = (statusIdx <= 0)
                            || t.getStatus().getDisplayName().equalsIgnoreCase(statusVal);
                    boolean matchesPriority = (priorityIdx <= 0)
                            || t.getPriority().getDisplayName().equalsIgnoreCase(priorityVal);
                    boolean matchesCategory = (categoryIdx <= 0)
                            || t.getCategory().equalsIgnoreCase(categoryVal);
                    boolean matchesAssignee = (assigneeIdx <= 0)
                            || t.getAssignedTechnician().equalsIgnoreCase(assigneeVal);
                    return matchesQuery && matchesStatus && matchesPriority && matchesCategory && matchesAssignee;
                })
                .collect(Collectors.toList());

        tableModel.setTickets(filtered);
        countSummaryLabel.setText("Showing " + filtered.size() + " of " + allTickets.size() + " total tickets");
        listCardLayout.show(listContainer, filtered.isEmpty() ? CARD_EMPTY : CARD_TABLE);
    }
}