package helpdesksystemui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class Dashboardpanel extends JPanel {

    private final Ticketrepository repository;
    private final Ticketsummarycard totalCard;
    private final Ticketsummarycard openCard;
    private final Ticketsummarycard inProgressCard;
    private final Ticketsummarycard resolvedCard;
    private final Ticketsummarycard closedCard;
    private final Recentticketpanel recentTicketPanel;

    public Dashboardpanel(Ticketrepository repository) {
        this(repository, null);
    }

    public Dashboardpanel(Ticketrepository repository, Consumer<Ticket> onViewTicket) {
        this.repository = repository;

        setLayout(new BorderLayout(0, 20));
        setBackground(Uitheme.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Top 5 Summary Cards Row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 5, 14, 0));
        cardsPanel.setOpaque(false);

        totalCard = new Ticketsummarycard("TOTAL TICKETS", 0, Uitheme.ACCENT_TOTAL);
        openCard = new Ticketsummarycard("OPEN TICKETS", 0, Uitheme.ACCENT_OPEN);
        inProgressCard = new Ticketsummarycard("IN PROGRESS", 0, Uitheme.ACCENT_IN_PROGRESS);
        resolvedCard = new Ticketsummarycard("RESOLVED", 0, Uitheme.ACCENT_RESOLVED);
        closedCard = new Ticketsummarycard("CLOSED", 0, Uitheme.ACCENT_CLOSED);

        cardsPanel.add(totalCard);
        cardsPanel.add(openCard);
        cardsPanel.add(inProgressCard);
        cardsPanel.add(resolvedCard);
        cardsPanel.add(closedCard);

        // Recent Tickets Section
        recentTicketPanel = new Recentticketpanel(onViewTicket);

        add(cardsPanel, BorderLayout.NORTH);
        add(recentTicketPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void setOnViewTicket(Consumer<Ticket> onViewTicket) {
        recentTicketPanel.setOnViewTicket(onViewTicket);
    }

    public final void refreshData() {
        if (repository == null) {
            return;
        }
        totalCard.setValue(repository.countAll());
        openCard.setValue(repository.countByStatus(Ticketstatus.OPEN));
        inProgressCard.setValue(repository.countByStatus(Ticketstatus.IN_PROGRESS));
        resolvedCard.setValue(repository.countByStatus(Ticketstatus.RESOLVED));
        closedCard.setValue(repository.countByStatus(Ticketstatus.CLOSED));
        recentTicketPanel.setTickets(repository.findRecent(10));
    }
}