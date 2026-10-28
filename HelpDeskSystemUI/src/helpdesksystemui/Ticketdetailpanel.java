package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Ticketdetailpanel extends JPanel {

    private final Ticketrepository repository;
    private final Runnable onDataUpdated;
    private final Runnable onBackAction;

    private Ticket currentTicket;

    private final JLabel headerIdLabel;
    private final JLabel subjectLabel;
    private final JLabel metaLabel;
    private final JLabel priorityBadgeLabel;
    private final JLabel statusBadgeLabel;
    private final JTextArea descriptionArea;
    private final JButton attachmentButton;
    private final DefaultListModel<String> activityListModel;
    private final JComboBox<String> technicianCombo;
    private final JComboBox<Ticketstatus> statusCombo;
    private final JComboBox<Ticketpriority> priorityCombo;
    private final JCheckBox staffModeCheckBox;
    private final JTextField commentField;

    public Ticketdetailpanel(
            Ticketrepository repository,
            Runnable onDataUpdated,
            Runnable onBackAction) {
        this.repository = repository;
        this.onDataUpdated = onDataUpdated;
        this.onBackAction = onBackAction;

        setLayout(new BorderLayout(0, 14));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Uitheme.CARD_BG);
        card.setBorder(Uitheme.createCardBorder());

        // Top Header with Back Button, Ticket ID, Subject, Category, and Badges (Slide 5)
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);

        JPanel navRow = new JPanel(new BorderLayout());
        navRow.setOpaque(false);

        JButton backButton = new JButton("← Back to List");
        backButton.setFont(Uitheme.FONT_BODY_BOLD);
        backButton.setBackground(Color.WHITE);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        backButton.addActionListener(e -> {
            if (this.onBackAction != null) {
                this.onBackAction.run();
            }
        });

        headerIdLabel = new JLabel("Ticket Details — HD-00000");
        headerIdLabel.setFont(Uitheme.FONT_HEADER);
        headerIdLabel.setForeground(Uitheme.TEXT_PRIMARY);
        headerIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftNav.setOpaque(false);
        leftNav.add(backButton);
        leftNav.add(headerIdLabel);

        // Badges on right
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgeRow.setOpaque(false);

        priorityBadgeLabel = new JLabel("HIGH");
        priorityBadgeLabel.setOpaque(true);
        priorityBadgeLabel.setFont(Uitheme.FONT_BADGE);
        priorityBadgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        statusBadgeLabel = new JLabel("OPEN");
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setFont(Uitheme.FONT_BADGE);
        statusBadgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        badgeRow.add(priorityBadgeLabel);
        badgeRow.add(statusBadgeLabel);

        navRow.add(leftNav, BorderLayout.WEST);
        navRow.add(badgeRow, BorderLayout.EAST);

        JPanel titleMetaPanel = new JPanel(new BorderLayout(0, 4));
        titleMetaPanel.setOpaque(false);
        titleMetaPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Uitheme.BORDER_COLOR));

        subjectLabel = new JLabel("Ticket Subject");
        subjectLabel.setFont(Uitheme.FONT_SECTION);
        subjectLabel.setForeground(Uitheme.TEXT_PRIMARY);

        metaLabel = new JLabel("• Category: Hardware | Requester: — | Created: —");
        metaLabel.setFont(Uitheme.FONT_SMALL);
        metaLabel.setForeground(Uitheme.TEXT_SECONDARY);
        metaLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        titleMetaPanel.add(subjectLabel, BorderLayout.NORTH);
        titleMetaPanel.add(metaLabel, BorderLayout.SOUTH);

        topSection.add(navRow, BorderLayout.NORTH);
        topSection.add(titleMetaPanel, BorderLayout.SOUTH);

        // Center Content: Left (Description + Attachment + Triage Controls) & Right (Activity & Comments)
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        centerSplit.setOpaque(false);

        // Left Column
        JPanel leftColumn = new JPanel(new BorderLayout(0, 12));
        leftColumn.setOpaque(false);

        JPanel descPanel = new JPanel(new BorderLayout(0, 6));
        descPanel.setOpaque(false);
        JLabel descHeader = new JLabel("Description");
        descHeader.setFont(Uitheme.FONT_BODY_BOLD);
        descHeader.setForeground(Uitheme.TEXT_PRIMARY);

        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Uitheme.FONT_BODY);
        descriptionArea.setBackground(new Color(248, 250, 252));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR));

        attachmentButton = new JButton("No attachment");
        attachmentButton.setFont(Uitheme.FONT_SMALL);
        attachmentButton.setHorizontalAlignment(JButton.LEFT);
        attachmentButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        attachmentButton.addActionListener(e -> handleOpenAttachment());

        descPanel.add(descHeader, BorderLayout.NORTH);
        descPanel.add(descScroll, BorderLayout.CENTER);
        descPanel.add(attachmentButton, BorderLayout.SOUTH);

        // Staff Assignment & Status Update Box (Slide 5 & Slide 13 Demo Checklist)
        JPanel managementBox = new JPanel(new BorderLayout(0, 10));
        managementBox.setBackground(new Color(248, 250, 252));
        managementBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        staffModeCheckBox = new JCheckBox("Demo Staff Mode (Uncheck to simulate Requester permission check)", true);
        staffModeCheckBox.setOpaque(false);
        staffModeCheckBox.setFont(Uitheme.FONT_SMALL);
        staffModeCheckBox.setForeground(Uitheme.TEXT_SECONDARY);

        JPanel controlsGrid = new JPanel(new GridLayout(3, 2, 10, 8));
        controlsGrid.setOpaque(false);

        JLabel techLabel = new JLabel("Assigned technician:");
        techLabel.setFont(Uitheme.FONT_BODY_BOLD);
        technicianCombo = new JComboBox<>(Demoticketrepository.TECHNICIANS);
        technicianCombo.setFont(Uitheme.FONT_BODY);

        JLabel statusUpdateLabel = new JLabel("Ticket Status:");
        statusUpdateLabel.setFont(Uitheme.FONT_BODY_BOLD);
        statusCombo = new JComboBox<>(Ticketstatus.values());
        statusCombo.setFont(Uitheme.FONT_BODY);

        JLabel priorityUpdateLabel = new JLabel("Ticket Priority:");
        priorityUpdateLabel.setFont(Uitheme.FONT_BODY_BOLD);
        priorityCombo = new JComboBox<>(Ticketpriority.values());
        priorityCombo.setFont(Uitheme.FONT_BODY);

        controlsGrid.add(techLabel);
        controlsGrid.add(technicianCombo);
        controlsGrid.add(statusUpdateLabel);
        controlsGrid.add(statusCombo);
        controlsGrid.add(priorityUpdateLabel);
        controlsGrid.add(priorityCombo);

        JButton saveUpdatesBtn = new JButton("Save Ticket Updates");
        saveUpdatesBtn.setFont(Uitheme.FONT_BODY_BOLD);
        saveUpdatesBtn.setBackground(Uitheme.PRIMARY);
        saveUpdatesBtn.setForeground(Color.WHITE);
        saveUpdatesBtn.setFocusPainted(false);
        saveUpdatesBtn.setBorderPainted(false);
        saveUpdatesBtn.setOpaque(true);
        saveUpdatesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveUpdatesBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        saveUpdatesBtn.addActionListener(e -> handleSaveTriageUpdates());

        managementBox.add(staffModeCheckBox, BorderLayout.NORTH);
        managementBox.add(controlsGrid, BorderLayout.CENTER);
        managementBox.add(saveUpdatesBtn, BorderLayout.SOUTH);

        leftColumn.add(descPanel, BorderLayout.CENTER);
        leftColumn.add(managementBox, BorderLayout.SOUTH);

        // Right Column: Activity & comments + Add comment (Slide 5)
        JPanel rightColumn = new JPanel(new BorderLayout(0, 10));
        rightColumn.setOpaque(false);

        JLabel activityHeader = new JLabel("Activity & comments");
        activityHeader.setFont(Uitheme.FONT_BODY_BOLD);
        activityHeader.setForeground(Uitheme.TEXT_PRIMARY);

        activityListModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(activityListModel);
        activityList.setFont(Uitheme.FONT_BODY);
        activityList.setFixedCellHeight(30);
        activityList.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane activityScroll = new JScrollPane(activityList);
        activityScroll.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR));

        JPanel commentBox = new JPanel(new BorderLayout(8, 6));
        commentBox.setOpaque(false);

        JLabel addCommentLabel = new JLabel("Add comment");
        addCommentLabel.setFont(Uitheme.FONT_BODY_BOLD);
        addCommentLabel.setForeground(Uitheme.TEXT_PRIMARY);

        commentField = new JTextField();
        commentField.setFont(Uitheme.FONT_BODY);
        commentField.setBorder(Uitheme.createInputBorder());
        commentField.setToolTipText("Write an update...");
        commentField.addActionListener(e -> handleSendComment());

        JButton sendCommentBtn = new JButton("Send Comment");
        sendCommentBtn.setFont(Uitheme.FONT_BODY_BOLD);
        sendCommentBtn.setBackground(Uitheme.PRIMARY);
        sendCommentBtn.setForeground(Color.WHITE);
        sendCommentBtn.setFocusPainted(false);
        sendCommentBtn.setBorderPainted(false);
        sendCommentBtn.setOpaque(true);
        sendCommentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendCommentBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        sendCommentBtn.addActionListener(e -> handleSendComment());

        commentBox.add(addCommentLabel, BorderLayout.NORTH);
        commentBox.add(commentField, BorderLayout.CENTER);
        commentBox.add(sendCommentBtn, BorderLayout.EAST);

        rightColumn.add(activityHeader, BorderLayout.NORTH);
        rightColumn.add(activityScroll, BorderLayout.CENTER);
        rightColumn.add(commentBox, BorderLayout.SOUTH);

        centerSplit.add(leftColumn);
        centerSplit.add(rightColumn);

        card.add(topSection, BorderLayout.NORTH);
        card.add(centerSplit, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public boolean loadTicketById(String ticketId) {
        if (repository == null || ticketId == null) {
            return false;
        }
        Optional<Ticket> found = repository.findById(ticketId);
        if (!found.isPresent()) {
            return false;
        }
        this.currentTicket = found.get();
        populateView();
        return true;
    }

    private void populateView() {
        if (currentTicket == null) {
            return;
        }
        headerIdLabel.setText("Ticket Details — " + currentTicket.getId());
        subjectLabel.setText(currentTicket.getTitle());
        metaLabel.setText(
                "• Category: " + currentTicket.getCategory()
                + "   |   Requester: " + currentTicket.getRequester()
                + "   |   Created: " + currentTicket.getCreatedDate()
        );

        stylePriorityBadge(currentTicket.getPriority());
        styleStatusBadge(currentTicket.getStatus());

        descriptionArea.setText(currentTicket.getDescription());
        descriptionArea.setCaretPosition(0);

        String attachment = currentTicket.getAttachmentPath();
        if (attachment == null || attachment.trim().isEmpty()) {
            attachmentButton.setText("Attachment: None attached");
            attachmentButton.setEnabled(false);
        } else {
            File file = new File(attachment);
            attachmentButton.setText("Attachment: " + file.getName() + " (Click to open/inspect)");
            attachmentButton.setEnabled(true);
        }

        technicianCombo.setSelectedItem(currentTicket.getAssignedTechnician());
        statusCombo.setSelectedItem(currentTicket.getStatus());
        priorityCombo.setSelectedItem(currentTicket.getPriority());

        activityListModel.clear();
        for (String entry : currentTicket.getActivityLog()) {
            activityListModel.addElement(entry);
        }
    }

    private void stylePriorityBadge(Ticketpriority priority) {
        priorityBadgeLabel.setText(priority.getDisplayName().toUpperCase());
        switch (priority) {
            case URGENT:
            case HIGH:
                priorityBadgeLabel.setBackground(Uitheme.BADGE_HIGH_BG);
                priorityBadgeLabel.setForeground(Uitheme.BADGE_HIGH_FG);
                break;
            case LOW:
                priorityBadgeLabel.setBackground(Uitheme.BADGE_LOW_BG);
                priorityBadgeLabel.setForeground(Uitheme.BADGE_LOW_FG);
                break;
            case MEDIUM:
            default:
                priorityBadgeLabel.setBackground(Uitheme.BADGE_MEDIUM_BG);
                priorityBadgeLabel.setForeground(Uitheme.BADGE_MEDIUM_FG);
                break;
        }
    }

    private void styleStatusBadge(Ticketstatus status) {
        statusBadgeLabel.setText(status.getDisplayName().toUpperCase());
        switch (status) {
            case IN_PROGRESS:
                statusBadgeLabel.setBackground(Uitheme.BADGE_PROGRESS_BG);
                statusBadgeLabel.setForeground(Uitheme.BADGE_PROGRESS_FG);
                break;
            case RESOLVED:
                statusBadgeLabel.setBackground(Uitheme.BADGE_RESOLVED_BG);
                statusBadgeLabel.setForeground(Uitheme.BADGE_RESOLVED_FG);
                break;
            case CLOSED:
                statusBadgeLabel.setBackground(Uitheme.BADGE_CLOSED_BG);
                statusBadgeLabel.setForeground(Uitheme.BADGE_CLOSED_FG);
                break;
            case OPEN:
            default:
                statusBadgeLabel.setBackground(Uitheme.BADGE_OPEN_BG);
                statusBadgeLabel.setForeground(Uitheme.BADGE_OPEN_FG);
                break;
        }
    }

    private void handleSaveTriageUpdates() {
        if (currentTicket == null) {
            return;
        }
        // Supports Slide 13 demo requirement: "Try an unauthorized action as Requester; it must be blocked"
        if (!staffModeCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Action blocked: Only Help Desk Staff can change ticket assignment, status, or priority.\n"
                    + "(Enable 'Demo Staff Mode' to perform staff triage actions.)",
                    "Permission Denied (Demo Role Check)",
                    JOptionPane.WARNING_MESSAGE
            );
            populateView();
            return;
        }

        String newTech = technicianCombo.getSelectedItem() == null
                ? "Unassigned"
                : technicianCombo.getSelectedItem().toString();
        Ticketstatus newStatus = (Ticketstatus) statusCombo.getSelectedItem();
        Ticketpriority newPriority = (Ticketpriority) priorityCombo.getSelectedItem();

        boolean changed = false;

        if (!newTech.equalsIgnoreCase(currentTicket.getAssignedTechnician())) {
            currentTicket.setAssignedTechnician(newTech);
            currentTicket.addActivity("Assigned to " + newTech);
            changed = true;
        }
        if (newStatus != null && newStatus != currentTicket.getStatus()) {
            currentTicket.setStatus(newStatus);
            currentTicket.addActivity("Status changed to " + newStatus.getDisplayName());
            changed = true;
        }
        if (newPriority != null && newPriority != currentTicket.getPriority()) {
            currentTicket.setPriority(newPriority);
            currentTicket.addActivity("Priority updated to " + newPriority.getDisplayName());
            changed = true;
        }

        if (!changed) {
            JOptionPane.showMessageDialog(
                    this,
                    "No changes were detected in technician assignment, status, or priority.",
                    "No Changes",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        boolean saved = repository.updateTicket(currentTicket);
        if (!saved) {
            JOptionPane.showMessageDialog(
                    this,
                    "The ticket was not saved. Check the repository and retry.",
                    "Unable to save",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        populateView();
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
        JOptionPane.showMessageDialog(
                this,
                "Ticket " + currentTicket.getId() + " has been updated.",
                "Ticket Updated",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleSendComment() {
        if (currentTicket == null) {
            return;
        }
        String comment = commentField.getText() == null ? "" : commentField.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a comment before clicking Send Comment.",
                    "Empty Comment",
                    JOptionPane.WARNING_MESSAGE
            );
            commentField.requestFocusInWindow();
            return;
        }

        currentTicket.addActivity(comment);
        boolean saved = repository.updateTicket(currentTicket);
        if (!saved) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save comment to the ticket record.",
                    "Unable to save",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        commentField.setText("");
        populateView();
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
    }

    private void handleOpenAttachment() {
        if (currentTicket == null || currentTicket.getAttachmentPath().isEmpty()) {
            return;
        }
        File file = new File(currentTicket.getAttachmentPath());
        if (file.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to open attachment with system viewer:\n" + ex.getMessage(),
                        "Attachment Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }
        JOptionPane.showMessageDialog(
                this,
                "Attachment File: " + file.getName() + "\nPath: " + currentTicket.getAttachmentPath(),
                "Attachment Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}