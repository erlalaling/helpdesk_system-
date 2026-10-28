package helpdesksystemui;

public enum Page {
    DASHBOARD("Dashboard", true),
    SUBMIT_TICKET("Submit Ticket", true),
    MY_TICKETS("My Tickets", true),
    ALL_TICKETS("All Tickets", true),
    REPORTS("Reports", true),
    TICKET_DETAILS("Ticket Details", false);

    private final String label;
    private final boolean sidebarVisible;

    Page(String label, boolean sidebarVisible) {
        this.label = label;
        this.sidebarVisible = sidebarVisible;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSidebarVisible() {
        return sidebarVisible;
    }
}