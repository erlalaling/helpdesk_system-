package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Mainframe extends JFrame {

    private final Ticketrepository repository;
    private final Sidebarpanel sidebarPanel;
    private final CardLayout cardLayout;
    private final JPanel contentCardsPanel;

    private final Dashboardpanel dashboardPanel;
    private final Submitticketpanel submitTicketPanel;
    private final Myticketspanel myTicketsPanel;
    private final Allticketspanel allTicketsPanel;
    private final Ticketdetailpanel ticketDetailsPanel;
    private final Reportspanel reportsPanel;

    private Page currentPage = Page.DASHBOARD;
    private Page previousListPage = Page.DASHBOARD;

    public Mainframe() {
        this(new Demoticketrepository());
    }

    public Mainframe(Ticketrepository repository) {
        super("Help Desk Ticket Management System");
        this.repository = repository == null ? new Demoticketrepository() : repository;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1180, 740));
        setMinimumSize(new Dimension(1024, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        sidebarPanel = new Sidebarpanel(this::navigateToPage);

        // Main Right Shell (Top Header + CardLayout Content)
        JPanel mainRightPanel = new JPanel(new BorderLayout());
        mainRightPanel.setBackground(Uitheme.CONTENT_BG);

        JPanel headerPanel = createHeaderPanel();
        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setBackground(Uitheme.CONTENT_BG);

        // Initialize all required screens
        dashboardPanel = new Dashboardpanel(
                this.repository,
                ticket -> openTicketDetails(ticket, Page.DASHBOARD)
        );

        submitTicketPanel = new Submitticketpanel(
                this.repository,
                createdTicket -> {
                    refreshAllViews();
                    openTicketDetails(createdTicket, Page.MY_TICKETS);
                },
                this::refreshAllViews
        );

        myTicketsPanel = new Myticketspanel(
                this.repository,
                ticket -> openTicketDetails(ticket, Page.MY_TICKETS)
        );

        allTicketsPanel = new Allticketspanel(
                this.repository,
                ticket -> openTicketDetails(ticket, Page.ALL_TICKETS)
        );

        ticketDetailsPanel = new Ticketdetailpanel(
                this.repository,
                this::refreshAllViews,
                () -> navigateToPage(previousListPage)
        );

        reportsPanel = new Reportspanel(this.repository);

        // Register all cards in CardLayout
        contentCardsPanel.add(dashboardPanel, Page.DASHBOARD.name());
        contentCardsPanel.add(submitTicketPanel, Page.SUBMIT_TICKET.name());
        contentCardsPanel.add(myTicketsPanel, Page.MY_TICKETS.name());
        contentCardsPanel.add(allTicketsPanel, Page.ALL_TICKETS.name());
        contentCardsPanel.add(ticketDetailsPanel, Page.TICKET_DETAILS.name());
        contentCardsPanel.add(reportsPanel, Page.REPORTS.name());

        mainRightPanel.add(headerPanel, BorderLayout.NORTH);
        mainRightPanel.add(contentCardsPanel, BorderLayout.CENTER);

        add(sidebarPanel, BorderLayout.WEST);
        add(mainRightPanel, BorderLayout.CENTER);

        navigateToPage(Page.DASHBOARD);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Uitheme.HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Uitheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel titleLabel = new JLabel("TICKET MANAGEMENT SYSTEM");
        titleLabel.setFont(Uitheme.FONT_HEADER);
        titleLabel.setForeground(Uitheme.TEXT_PRIMARY);

        JLabel userBadge = new JLabel("Requester: " + Demoticketrepository.DEMO_CURRENT_REQUESTER + " (Demo Session)");
        userBadge.setFont(Uitheme.FONT_SMALL);
        userBadge.setForeground(Uitheme.TEXT_SECONDARY);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(userBadge, BorderLayout.EAST);
        return header;
    }

    public final void navigateToPage(Page destination) {
        if (destination == null) {
            return;
        }
        // Check for unsaved changes on Submit Ticket form before leaving (Slide 10 rule)
        if (currentPage == Page.SUBMIT_TICKET
                && destination != Page.SUBMIT_TICKET
                && submitTicketPanel.hasUnsavedInput()) {
            Object[] options = {"Stay", "Discard"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "You have unsaved changes. Leave this page?",
                    "Discard changes?",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (choice != 1) {
                sidebarPanel.setActivePage(Page.SUBMIT_TICKET);
                return;
            }
            submitTicketPanel.clearFormSilently();
        }

        currentPage = destination;
        if (destination.isSidebarVisible()) {
            sidebarPanel.setActivePage(destination);
        }
        refreshAllViews();
        cardLayout.show(contentCardsPanel, destination.name());
    }

    public void openTicketDetails(Ticket ticket, Page sourcePage) {
        if (ticket == null || ticket.getId() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load the selected ticket. The ticket ID is missing or invalid.",
                    "Ticket Not Found",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        this.previousListPage = sourcePage == null ? Page.DASHBOARD : sourcePage;
        if (this.previousListPage.isSidebarVisible()) {
            sidebarPanel.setActivePage(this.previousListPage);
        }
        boolean loaded = ticketDetailsPanel.loadTicketById(ticket.getId());
        if (!loaded) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ticket " + ticket.getId() + " could not be found in the repository.",
                    "Ticket Not Found",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        currentPage = Page.TICKET_DETAILS;
        cardLayout.show(contentCardsPanel, Page.TICKET_DETAILS.name());
    }

    public void refreshAllViews() {
        dashboardPanel.refreshData();
        myTicketsPanel.refreshData();
        allTicketsPanel.refreshData();
        reportsPanel.refreshData();
    }
}