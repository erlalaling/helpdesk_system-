package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.function.Consumer;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Submitticketpanel extends JPanel {

    private final Ticketrepository repository;
    private final Consumer<Ticket> onViewCreatedTicket;
    private final Runnable onTicketSaved;

    private final JTextField titleField;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<Ticketpriority> priorityCombo;
    private final JTextArea descriptionArea;
    private final JScrollPane descriptionScroll;
    private final JLabel attachmentLabel;
    private final JLabel validationBanner;

    private String selectedAttachmentPath = "";

    public Submitticketpanel(
            Ticketrepository repository,
            Consumer<Ticket> onViewCreatedTicket,
            Runnable onTicketSaved) {
        this.repository = repository;
        this.onViewCreatedTicket = onViewCreatedTicket;
        this.onTicketSaved = onTicketSaved;

        setLayout(new BorderLayout(0, 16));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Card Wrapper
        JPanel formCard = new JPanel(new BorderLayout(0, 18));
        formCard.setBackground(Uitheme.CARD_BG);
        formCard.setBorder(Uitheme.createCardBorder());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Submit a Ticket");
        titleLabel.setFont(Uitheme.FONT_HEADER);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Describe your issue in detail");
        subtitleLabel.setFont(Uitheme.FONT_BODY);
        subtitleLabel.setForeground(Uitheme.TEXT_SECONDARY);

        validationBanner = new JLabel(" ");
        validationBanner.setFont(Uitheme.FONT_BODY_BOLD);
        validationBanner.setForeground(Uitheme.DANGER);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        headerPanel.add(validationBanner, BorderLayout.SOUTH);

        // Form Fields Grid
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Row 0: Title *
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        fieldsPanel.add(createFieldLabel("Title *"), gbc);

        titleField = new JTextField();
        titleField.setFont(Uitheme.FONT_BODY);
        titleField.setBorder(Uitheme.createInputBorder());
        titleField.setToolTipText("Enter a concise summary of your issue (e.g., Computer not turning on)");
        gbc.gridy = 1;
        fieldsPanel.add(titleField, gbc);

        // Row 2: Category * and Priority
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        fieldsPanel.add(createFieldLabel("Category *"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);
        fieldsPanel.add(createFieldLabel("Priority"), gbc);

        String[] categoryOptions = new String[Demoticketrepository.CATEGORIES.length + 1];
        categoryOptions[0] = "-- Select Category --";
        System.arraycopy(Demoticketrepository.CATEGORIES, 0, categoryOptions, 1, Demoticketrepository.CATEGORIES.length);
        categoryCombo = new JComboBox<>(categoryOptions);
        categoryCombo.setSelectedIndex(0); // Default to "Hardware" matching Slide 2
        categoryCombo.setFont(Uitheme.FONT_BODY);

        priorityCombo = new JComboBox<>(Ticketpriority.values());
        priorityCombo.setSelectedItem(Ticketpriority.DEFAULT); // Default matching Slide 2
        priorityCombo.setFont(Uitheme.FONT_BODY);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 0, 6, 16);
        fieldsPanel.add(categoryCombo, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);
        fieldsPanel.add(priorityCombo, gbc);

        // Row 4: Description *
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        fieldsPanel.add(createFieldLabel("Description *"), gbc);

        descriptionArea = new JTextArea(6, 40);
        descriptionArea.setFont(Uitheme.FONT_BODY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR, 1));

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        fieldsPanel.add(descriptionScroll, gbc);

        // Row 6: Attachment Picker (Choose File / No file selected)
        JPanel attachmentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        attachmentRow.setOpaque(false);

        JButton chooseFileButton = new JButton("Choose File");
        chooseFileButton.setFont(Uitheme.FONT_BODY);
        chooseFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chooseFileButton.addActionListener(e -> handleChooseFile());

        attachmentLabel = new JLabel("No file selected");
        attachmentLabel.setFont(Uitheme.FONT_SMALL);
        attachmentLabel.setForeground(Uitheme.TEXT_SECONDARY);

        attachmentRow.add(chooseFileButton);
        attachmentRow.add(attachmentLabel);

        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 6, 0);
        fieldsPanel.add(attachmentRow, gbc);

        // Footer Buttons (Submit Ticket ➤ and Reset)
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonBar.setOpaque(false);

        JButton submitButton = new JButton("Submit Ticket ➤");
        submitButton.setFont(Uitheme.FONT_BODY_BOLD);
        submitButton.setBackground(Uitheme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setOpaque(true);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(e -> handleSubmit());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(Uitheme.FONT_BODY);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
        resetButton.setBackground(Color.WHITE);
        resetButton.addActionListener(e -> handleResetWithConfirmation());

        buttonBar.add(submitButton);
        buttonBar.add(resetButton);

        formCard.add(headerPanel, BorderLayout.NORTH);
        formCard.add(fieldsPanel, BorderLayout.CENTER);
        formCard.add(buttonBar, BorderLayout.SOUTH);

        add(formCard, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Uitheme.FONT_BODY_BOLD);
        label.setForeground(Uitheme.TEXT_PRIMARY);
        return label;
    }

    private void handleChooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Attachment for Ticket");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            File file = chooser.getSelectedFile();
            selectedAttachmentPath = file.getAbsolutePath();
            attachmentLabel.setText(file.getName());
            attachmentLabel.setForeground(Uitheme.TEXT_PRIMARY);
        }
    }

    private void resetValidationBorders() {
        titleField.setBorder(Uitheme.createInputBorder());
        descriptionScroll.setBorder(BorderFactory.createLineBorder(Uitheme.BORDER_COLOR, 1));
        validationBanner.setText(" ");
    }

    private void handleSubmit() {
        resetValidationBorders();

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        int categoryIndex = categoryCombo.getSelectedIndex();
        String category = categoryCombo.getSelectedItem() == null ? "" : categoryCombo.getSelectedItem().toString();
        Ticketpriority priority = (Ticketpriority) priorityCombo.getSelectedItem();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();

        // Required-field validation pointing directly to the field needing attention (Slide 10)
        if (title.isEmpty()) {
            titleField.setBorder(Uitheme.createErrorInputBorder());
            validationBanner.setText("Validation Error: Title * is required. Please enter a ticket title.");
            titleField.requestFocusInWindow();
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a Ticket Title before submitting.",
                    "Unable to submit",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (categoryIndex <= 0 || category.isEmpty()) {
            validationBanner.setText("Validation Error: Category * is required. Please choose a category.");
            categoryCombo.requestFocusInWindow();
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a valid Category before submitting.",
                    "Unable to submit",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (description.isEmpty()) {
            descriptionScroll.setBorder(BorderFactory.createLineBorder(Uitheme.DANGER, 2));
            validationBanner.setText("Validation Error: Description * is required. Please describe the issue.");
            descriptionArea.requestFocusInWindow();
            JOptionPane.showMessageDialog(
                    this,
                    "Please provide a Description of your issue before submitting.",
                    "Unable to submit",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (repository == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The ticket was not submitted. Repository is unavailable.",
                    "Unable to submit",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Create and save in the shared repository BEFORE showing success feedback
        Ticket created = repository.createTicket(
                title,
                description,
                category,
                priority == null ? Ticketpriority.MEDIUM : priority,
                Demoticketrepository.DEMO_CURRENT_REQUESTER,
                selectedAttachmentPath, computeMinute()
        );

        if (created == null || !repository.findById(created.getId()).isPresent()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The ticket was not saved. Check ticket details and retry.",
                    "Unable to save",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Clear form only AFTER repository confirms creation
        clearFormSilently();

        if (onTicketSaved != null) {
            onTicketSaved.run();
        }

        // Confirmation dialog matching Slide 10 ("Ticket submitted! Ticket ID: HD-00025 ... [View Ticket] [OK]")
        Object[] options = {"View Ticket", "Stay Here"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Ticket ID: " + created.getId() + "\nYour request has been recorded.",
                "Ticket submitted!",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0 && onViewCreatedTicket != null) {
            onViewCreatedTicket.accept(created);
        }
    }

    private void handleResetWithConfirmation() {
        if (hasUnsavedInput()) {
            Object[] options = {"Stay", "Discard"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "You have unsaved changes. Clear all entered fields?",
                    "Discard changes?",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (choice != 1) {
                return;
            }
        }
        clearFormSilently();
    }

    public boolean hasUnsavedInput() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        return !title.isEmpty() || !description.isEmpty() || !selectedAttachmentPath.isEmpty();
    }

    public void clearFormSilently() {
        resetValidationBorders();
        titleField.setText("");
        categoryCombo.setSelectedIndex(0);
        priorityCombo.setSelectedItem(Ticketpriority.HIGH);
        descriptionArea.setText("");
        selectedAttachmentPath = "";
        attachmentLabel.setText("No file selected");
        attachmentLabel.setForeground(Uitheme.TEXT_SECONDARY);
    }


    public int computeMinute(){
        int totalMinutes = 0;
        try {
            Date now = new Date();
            int hours = now.getHours();
            int minutes = now.getMinutes();
            totalMinutes = hours * 60 + minutes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalMinutes;
    }
}