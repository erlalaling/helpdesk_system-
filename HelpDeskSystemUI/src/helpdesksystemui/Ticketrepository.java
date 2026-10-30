package helpdesksystemui;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Ticketrepository {

    List<Ticket> findAll();

    List<Ticket> findRecent(int limit);

    // Unfinished (OPEN / IN_PROGRESS) tickets in processing order:
    // oldest date, then earliest hour, then most urgent priority.
    List<Ticket> findPrioritized(int limit);

    // RESOLVED and CLOSED tickets.
    List<Ticket> findCompleted();

    List<Ticket> findByRequester(String requester);

    List<Ticket> findByDateRange(LocalDate fromDate, LocalDate toDate);

    Optional<Ticket> findById(String id);

    Ticket createTicket(
            String title,
            String description,
            String category,
            Ticketpriority priority,
            String requester,
            String attachmentPath, int totalTime);

    boolean save(Ticket ticket);

    boolean updateTicket(Ticket ticket);

    int countAll();

    int countByStatus(Ticketstatus status);
}