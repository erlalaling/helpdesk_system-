package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Reportspanel extends JPanel {

    private final Ticketrepository repository;

    private final Ticketsummarycard totalCard;
    private final Ticketsummarycard openCard;
    private final Ticketsummarycard resolvedCard;
    private final Ticketsummarycard urgentCard;

    private final JTextField fromDateField;
    private final JTextField toDateField;
    private final JLabel filterStatusLabel;

    private final StatusBarChartPanel statusChartPanel;
    private final CategoryBreakdownPanel categoryBreakdownPanel;

    private LocalDate activeFromDate = LocalDate.of(2026, 10, 1);
    private LocalDate activeToDate = LocalDate.now();

    public Reportspanel(Ticketrepository repository) {
        this.repository = repository;

        setLayout(new BorderLayout(0, 18));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Top Title + Summary Metric Cards (Slide 6: Total, Open, Resolved, Urgent)
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);

        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(Uitheme.FONT_HEADER);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsGrid.setOpaque(false);

        totalCard = new Ticketsummarycard("Total", 0, Uitheme.ACCENT_TOTAL);
        openCard = new Ticketsummarycard("Open", 0, Uitheme.ACCENT_OPEN);
        resolvedCard = new Ticketsummarycard("Resolved", 0, Uitheme.ACCENT_RESOLVED);
        urgentCard = new Ticketsummarycard("Urgent / High", 0, Uitheme.DANGER);

        cardsGrid.add(totalCard);
        cardsGrid.add(openCard);
        cardsGrid.add(resolvedCard);
        cardsGrid.add(urgentCard);

        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(cardsGrid, BorderLayout.CENTER);

        // Center Charts Area: Left = Tickets by Status (Slide 6), Right = Tickets by Category & Priority
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);

        statusChartPanel = new StatusBarChartPanel();
        categoryBreakdownPanel = new CategoryBreakdownPanel();

        chartsGrid.add(statusChartPanel);
        chartsGrid.add(categoryBreakdownPanel);

        // Bottom Report Filters Card (Slide 6: From date, To date, Apply, Export CSV)
        JPanel filterCard = new JPanel(new BorderLayout(0, 8));
        filterCard.setBackground(Uitheme.CARD_BG);
        filterCard.setBorder(Uitheme.createCardBorder());

        JLabel filterTitle = new JLabel("Report Filters");
        filterTitle.setFont(Uitheme.FONT_SECTION);
        filterTitle.setForeground(Uitheme.TEXT_PRIMARY);

        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsRow.setOpaque(false);

        JLabel fromLabel = new JLabel("From date (YYYY-MM-DD):");
        fromLabel.setFont(Uitheme.FONT_BODY_BOLD);
        fromDateField = new JTextField(activeFromDate.toString(), 11);
        fromDateField.setFont(Uitheme.FONT_BODY);
        fromDateField.setBorder(Uitheme.createInputBorder());

        JLabel toLabel = new JLabel("To date (YYYY-MM-DD):");
        toLabel.setFont(Uitheme.FONT_BODY_BOLD);
        toDateField = new JTextField(activeToDate.toString(), 11);
        toDateField.setFont(Uitheme.FONT_BODY);
        toDateField.setBorder(Uitheme.createInputBorder());

        JButton applyButton = new JButton("Apply");
        applyButton.setFont(Uitheme.FONT_BODY_BOLD);
        applyButton.setBackground(Uitheme.PRIMARY);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        applyButton.setBorderPainted(false);
        applyButton.setOpaque(true);
        applyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyButton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        applyButton.addActionListener(e -> handleApplyDateRange());

        JButton exportCsvButton = new JButton("Export CSV");
        exportCsvButton.setFont(Uitheme.FONT_BODY_BOLD);
        exportCsvButton.setBackground(Color.WHITE);
        exportCsvButton.setForeground(Uitheme.TEXT_PRIMARY);
        exportCsvButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportCsvButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)
        ));
        exportCsvButton.addActionListener(e -> handleExportCsv());

        filterStatusLabel = new JLabel();
        filterStatusLabel.setFont(Uitheme.FONT_SMALL);
        filterStatusLabel.setForeground(Uitheme.TEXT_SECONDARY);

        controlsRow.add(fromLabel);
        controlsRow.add(fromDateField);
        controlsRow.add(toLabel);
        controlsRow.add(toDateField);
        controlsRow.add(applyButton);
        controlsRow.add(exportCsvButton);
        controlsRow.add(filterStatusLabel);

        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(controlsRow, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(chartsGrid, BorderLayout.CENTER);
        add(filterCard, BorderLayout.SOUTH);

        refreshData();
    }

    private void handleApplyDateRange() {
        String fromText = fromDateField.getText() == null ? "" : fromDateField.getText().trim();
        String toText = toDateField.getText() == null ? "" : toDateField.getText().trim();

        try {
            LocalDate parsedFrom = fromText.isEmpty() ? null : LocalDate.parse(fromText);
            LocalDate parsedTo = toText.isEmpty() ? null : LocalDate.parse(toText);

            if (parsedFrom != null && parsedTo != null && parsedFrom.isAfter(parsedTo)) {
                JOptionPane.showMessageDialog(
                        this,
                        "'From date' (" + parsedFrom + ") cannot be later than 'To date' (" + parsedTo + ").",
                        "Invalid Date Range",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            this.activeFromDate = parsedFrom;
            this.activeToDate = parsedTo;
            refreshData();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid date format. Please enter dates in YYYY-MM-DD format (e.g., 2026-10-01).",
                    "Invalid Date Format",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public final void refreshData() {
        if (repository == null) {
            return;
        }
        List<Ticket> filtered = repository.findByDateRange(activeFromDate, activeToDate);

        int total = filtered.size();
        int open = 0;
        int inProgress = 0;
        int resolved = 0;
        int closed = 0;
        int urgentOrHigh = 0;

        Map<Ticketstatus, Integer> statusCounts = new EnumMap<>(Ticketstatus.class);
        for (Ticketstatus st : Ticketstatus.values()) {
            statusCounts.put(st, 0);
        }

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (String cat : Demoticketrepository.CATEGORIES) {
            categoryCounts.put(cat, 0);
        }

        for (Ticket t : filtered) {
            statusCounts.put(t.getStatus(), statusCounts.getOrDefault(t.getStatus(), 0) + 1);
            switch (t.getStatus()) {
                case OPEN:
                    open++;
                    break;
                case IN_PROGRESS:
                    inProgress++;
                    break;
                case RESOLVED:
                    resolved++;
                    break;
                case CLOSED:
                    closed++;
                    break;
                default:
                    break;
            }
            if (t.getPriority() == Ticketpriority.URGENT || t.getPriority() == Ticketpriority.HIGH) {
                urgentOrHigh++;
            }
            categoryCounts.put(t.getCategory(), categoryCounts.getOrDefault(t.getCategory(), 0) + 1);
        }

        totalCard.setValue(total);
        openCard.setValue(open);
        resolvedCard.setValue(resolved);
        urgentCard.setValue(urgentOrHigh);

        filterStatusLabel.setText(
                "Showing " + total + " tickets (In Progress: " + inProgress + ", Closed: " + closed + ")"
        );

        statusChartPanel.setStatusCounts(statusCounts, total);
        categoryBreakdownPanel.setCategoryCounts(categoryCounts, total);
    }

    private void handleExportCsv() {
        if (repository == null) {
            return;
        }
        List<Ticket> filtered = repository.findByDateRange(activeFromDate, activeToDate);
        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "There are no tickets in the selected date range to export.",
                    "Empty Dataset",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Ticket Report as CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setSelectedFile(new File("helpdesk_report_" + LocalDate.now() + ".csv"));

        int userSelection = chooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        File outputFile = chooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".csv")) {
            outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write("Ticket ID,Title,Category,Priority,Status,Requester,Assigned Technician,Created Date");
            writer.newLine();
            for (Ticket t : filtered) {
                writer.write(escapeCsv(t.getId()) + ","
                        + escapeCsv(t.getTitle()) + ","
                        + escapeCsv(t.getCategory()) + ","
                        + escapeCsv(t.getPriority().getDisplayName()) + ","
                        + escapeCsv(t.getStatus().getDisplayName()) + ","
                        + escapeCsv(t.getRequester()) + ","
                        + escapeCsv(t.getAssignedTechnician()) + ","
                        + escapeCsv(t.getCreatedDate() == null ? "" : t.getCreatedDate().toString()));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV file:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Exported " + filtered.size() + " tickets to:\n" + outputFile.getAbsolutePath(),
                "CSV Export Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    // Native Java2D Horizontal Bar Chart for "Tickets by Status" (Slide 6)
    private static class StatusBarChartPanel extends JPanel {

        private Map<Ticketstatus, Integer> counts = new EnumMap<>(Ticketstatus.class);
        private int total = 0;

        StatusBarChartPanel() {
            setBackground(Uitheme.CARD_BG);
            setBorder(Uitheme.createCardBorder());
            setPreferredSize(new Dimension(420, 250));
        }

        void setStatusCounts(Map<Ticketstatus, Integer> counts, int total) {
            this.counts = new EnumMap<>(counts);
            this.total = total;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padX = 20;
            int y = 30;

            g2.setFont(Uitheme.FONT_SECTION);
            g2.setColor(Uitheme.TEXT_PRIMARY);
            g2.drawString("Tickets by Status", padX, y);

            y += 28;
            int labelWidth = 100;
            int countWidth = 45;
            int maxBarWidth = Math.max(80, getWidth() - padX * 2 - labelWidth - countWidth);

            int maxVal = 1;
            for (Integer v : counts.values()) {
                if (v != null && v > maxVal) {
                    maxVal = v;
                }
            }

            Ticketstatus[] order = {
                Ticketstatus.OPEN,
                Ticketstatus.IN_PROGRESS,
                Ticketstatus.RESOLVED,
                Ticketstatus.CLOSED
            };

            for (Ticketstatus st : order) {
                int count = counts.getOrDefault(st, 0);
                g2.setFont(Uitheme.FONT_BODY);
                g2.setColor(Uitheme.TEXT_PRIMARY);
                g2.drawString(st.getDisplayName(), padX, y + 16);

                int barX = padX + labelWidth;
                int barHeight = 22;
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(barX, y, maxBarWidth, barHeight, 8, 8);

                int filledWidth = total == 0 ? 0 : (int) Math.round((count * 1.0 / maxVal) * maxBarWidth);
                g2.setColor(colorForStatus(st));
                if (filledWidth > 0) {
                    g2.fillRoundRect(barX, y, Math.max(8, filledWidth), barHeight, 8, 8);
                }

                g2.setFont(Uitheme.FONT_BODY_BOLD);
                g2.setColor(Uitheme.TEXT_PRIMARY);
                g2.drawString(String.valueOf(count), barX + maxBarWidth + 12, y + 16);

                y += 42;
            }
            g2.dispose();
        }

        private Color colorForStatus(Ticketstatus status) {
            switch (status) {
                case OPEN:
                    return Uitheme.ACCENT_OPEN;
                case IN_PROGRESS:
                    return Uitheme.ACCENT_IN_PROGRESS;
                case RESOLVED:
                    return Uitheme.ACCENT_RESOLVED;
                case CLOSED:
                default:
                    return Uitheme.ACCENT_CLOSED;
            }
        }
    }

    // Native Java2D Breakdown Chart for "Tickets by Category"
    private static class CategoryBreakdownPanel extends JPanel {

        private Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        private int total = 0;

        CategoryBreakdownPanel() {
            setBackground(Uitheme.CARD_BG);
            setBorder(Uitheme.createCardBorder());
            setPreferredSize(new Dimension(420, 250));
        }

        void setCategoryCounts(Map<String, Integer> categoryCounts, int total) {
            this.categoryCounts = new LinkedHashMap<>(categoryCounts);
            this.total = total;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padX = 20;
            int y = 30;

            g2.setFont(Uitheme.FONT_SECTION);
            g2.setColor(Uitheme.TEXT_PRIMARY);
            g2.drawString("Tickets by Category", padX, y);

            y += 28;
            int labelWidth = 100;
            int countWidth = 70;
            int maxBarWidth = Math.max(80, getWidth() - padX * 2 - labelWidth - countWidth);

            int maxVal = 1;
            for (Integer v : categoryCounts.values()) {
                if (v != null && v > maxVal) {
                    maxVal = v;
                }
            }

            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                String cat = entry.getKey();
                int count = entry.getValue() == null ? 0 : entry.getValue();

                g2.setFont(Uitheme.FONT_BODY);
                g2.setColor(Uitheme.TEXT_PRIMARY);
                g2.drawString(cat, padX, y + 15);

                int barX = padX + labelWidth;
                int barHeight = 20;
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(barX, y, maxBarWidth, barHeight, 8, 8);

                int filledWidth = total == 0 ? 0 : (int) Math.round((count * 1.0 / maxVal) * maxBarWidth);
                g2.setColor(Uitheme.PRIMARY);
                if (filledWidth > 0) {
                    g2.fillRoundRect(barX, y, Math.max(8, filledWidth), barHeight, 8, 8);
                }

                int pct = total == 0 ? 0 : (int) Math.round((count * 100.0) / total);
                String text = count + " (" + pct + "%)";
                FontMetrics fm = g2.getFontMetrics(Uitheme.FONT_SMALL);
                g2.setFont(Uitheme.FONT_SMALL);
                g2.setColor(Uitheme.TEXT_SECONDARY);
                g2.drawString(text, barX + maxBarWidth + 10, y + (barHeight + fm.getAscent()) / 2 - 2);

                y += 34;
            }
            g2.dispose();
        }
    }
}