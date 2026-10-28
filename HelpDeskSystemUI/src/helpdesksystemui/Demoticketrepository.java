package helpdesksystemui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory demonstration repository for the HelpDeskSystem project.
 * Note: Because this repository uses in-memory collections for classroom
 * demonstration, newly created or modified tickets persist across all screens
 * during the active session and reset when the application closes.
 */
public class Demoticketrepository implements Ticketrepository {

    public static final String DEMO_CURRENT_REQUESTER = "Sean Gerolaga";

    public static final String[] CATEGORIES = {
        "Hardware", "Network", "Account", "Software", "Facilities"
    };

    public static final String[] TECHNICIANS = {
        "Unassigned", "IT Support", "Mark Villanueva", "Jessa Mae Cruz", "Ryan Dela Cruz"
    };

    private final List<Ticket> tickets = new ArrayList<>();
    private int nextSequenceNumber = 25;

    public Demoticketrepository() {
        seedDemoTickets();
    }

    private void seedDemoTickets() {
        // Featured top 5 tickets matching the PPTX slides (HD-00024 to HD-00020)
        tickets.add(new Ticket(
                "HD-00024",
                "Computer not turning on",
                "Computer does not power on. Power cable checked and wall outlet verified.",
                "Hardware",
                Ticketpriority.HIGH,
                Ticketstatus.OPEN,
                DEMO_CURRENT_REQUESTER,
                "Unassigned",
                "power_supply_diagnostic.log",
                LocalDate.of(2026, 10, 24),
                Arrays.asList(
                        "10:45 AM — Ticket submitted",
                        "11:10 AM — Assigned to IT Support queue"
                )
        ));

        tickets.add(new Ticket(
                "HD-00023",
                "Internet connection issue",
                "Intermittent Wi-Fi disconnection in Computer Lab 2 during lab exercises.",
                "Network",
                Ticketpriority.MEDIUM,
                Ticketstatus.RESOLVED,
                "Maria Santos",
                "IT Support",
                "",
                LocalDate.of(2026, 10, 23),
                Arrays.asList(
                        "09:15 AM — Ticket submitted",
                        "09:40 AM — Assigned to IT Support",
                        "11:05 AM — Access point channel reconfigured and marked Resolved"
                )
        ));

        tickets.add(new Ticket(
                "HD-00022",
                "Reset password request",
                "Unable to log in to the student portal after multi-factor reset.",
                "Account",
                Ticketpriority.LOW,
                Ticketstatus.IN_PROGRESS,
                "Carlos Reyes",
                "Jessa Mae Cruz",
                "",
                LocalDate.of(2026, 10, 22),
                Arrays.asList(
                        "01:20 PM — Ticket submitted",
                        "01:45 PM — Assigned to Jessa Mae Cruz",
                        "02:00 PM — Identity verification in progress"
                )
        ));

        tickets.add(new Ticket(
                "HD-00021",
                "Software installation error",
                "Apache NetBeans JDK path configuration error on workstation PC-14.",
                "Software",
                Ticketpriority.MEDIUM,
                Ticketstatus.OPEN,
                "Ana Lopez",
                "Unassigned",
                "netbeans_error_screenshot.png",
                LocalDate.of(2026, 10, 21),
                Collections.singletonList("08:30 AM — Ticket submitted")
        ));

        tickets.add(new Ticket(
                "HD-00020",
                "Monitor not displaying",
                "External monitor shows 'No HDMI Signal' despite reseating display cable.",
                "Hardware",
                Ticketpriority.HIGH,
                Ticketstatus.RESOLVED,
                "Pedro Santos",
                "Mark Villanueva",
                "",
                LocalDate.of(2026, 10, 20),
                Arrays.asList(
                        "02:10 PM — Ticket submitted",
                        "02:30 PM — Assigned to Mark Villanueva",
                        "03:15 PM — Replaced faulty HDMI cable; display restored"
                )
        ));

        // Additional tickets for DEMO_CURRENT_REQUESTER ("Sean Gerolaga") matching My Tickets slide
        tickets.add(new Ticket(
                "HD-00019",
                "Internet connection issue",
                "Ethernet port at Lab Row 3 Seat 4 shows unplugged status.",
                "Network",
                Ticketpriority.MEDIUM,
                Ticketstatus.RESOLVED,
                DEMO_CURRENT_REQUESTER,
                "IT Support",
                "",
                LocalDate.of(2026, 10, 19),
                Arrays.asList(
                        "10:00 AM — Ticket submitted",
                        "11:30 AM — Patch cord replaced and marked Resolved"
                )
        ));

        tickets.add(new Ticket(
                "HD-00018",
                "Reset password request",
                "Requesting LMS account password reset for 1st Semester course access.",
                "Account",
                Ticketpriority.LOW,
                Ticketstatus.IN_PROGRESS,
                DEMO_CURRENT_REQUESTER,
                "Jessa Mae Cruz",
                "",
                LocalDate.of(2026, 10, 18),
                Arrays.asList(
                        "09:05 AM — Ticket submitted",
                        "09:30 AM — Assigned to Jessa Mae Cruz"
                )
        ));

        tickets.add(new Ticket(
                "HD-00017",
                "Software installation error",
                "Missing JavaFX SDK library on Lab PC-08.",
                "Software",
                Ticketpriority.MEDIUM,
                Ticketstatus.OPEN,
                DEMO_CURRENT_REQUESTER,
                "Unassigned",
                "",
                LocalDate.of(2026, 10, 17),
                Collections.singletonList("03:40 PM — Ticket submitted")
        ));

        tickets.add(new Ticket(
                "HD-00016",
                "Monitor not displaying",
                "Flickering LCD panel in Room 302.",
                "Hardware",
                Ticketpriority.HIGH,
                Ticketstatus.RESOLVED,
                DEMO_CURRENT_REQUESTER,
                "Mark Villanueva",
                "",
                LocalDate.of(2026, 10, 16),
                Arrays.asList(
                        "01:00 PM — Ticket submitted",
                        "02:45 PM — Display unit swapped and Resolved"
                )
        ));

        // Additional tickets to match the exact Dashboard totals from Slide 1:
        // Total = 46 (12 Open, 9 In Progress, 19 Resolved, 6 Closed)
        // Currently seeded above: 3 Open, 2 In Progress, 4 Resolved, 0 Closed (Total = 9)
        // Need: 9 more Open, 7 more In Progress, 15 more Resolved, 6 Closed (37 more = 46 total)
        String[] requesters = {"Maria Santos", "Carlos Reyes", "Ana Lopez", "Pedro Santos", DEMO_CURRENT_REQUESTER};
        int idCounter = 15;

        // 9 more OPEN tickets
        for (int i = 0; i < 9; i++) {
            Ticketpriority p = (i % 3 == 0) ? Ticketpriority.URGENT : (i % 2 == 0 ? Ticketpriority.HIGH : Ticketpriority.MEDIUM);
            tickets.add(createSeededTicket(idCounter--, "Workstation peripheral issue #" + (i + 1),
                    CATEGORIES[i % CATEGORIES.length], p, Ticketstatus.OPEN,
                    requesters[i % requesters.length], "Unassigned", LocalDate.of(2026, 10, Math.max(1, 15 - i))));
        }

        // 7 more IN_PROGRESS tickets
        for (int i = 0; i < 7; i++) {
            Ticketpriority p = (i == 0) ? Ticketpriority.URGENT : Ticketpriority.MEDIUM;
            tickets.add(createSeededTicket(idCounter--, "Network switch configuration #" + (i + 1),
                    CATEGORIES[(i + 1) % CATEGORIES.length], p, Ticketstatus.IN_PROGRESS,
                    requesters[i % requesters.length], TECHNICIANS[1 + (i % (TECHNICIANS.length - 1))],
                    LocalDate.of(2026, 10, Math.max(1, 14 - i))));
        }

        // 15 more RESOLVED tickets
        for (int i = 0; i < 15; i++) {
            Ticketpriority p = (i % 4 == 0) ? Ticketpriority.HIGH : Ticketpriority.LOW;
            tickets.add(createSeededTicket(idCounter--, "Completed lab maintenance request #" + (i + 1),
                    CATEGORIES[(i + 2) % CATEGORIES.length], p, Ticketstatus.RESOLVED,
                    requesters[i % requesters.length], TECHNICIANS[1 + (i % (TECHNICIANS.length - 1))],
                    LocalDate.of(2026, 10, Math.max(1, 15 - (i % 14)))));
        }

        // 6 CLOSED tickets
        for (int i = 0; i < 6; i++) {
            tickets.add(createSeededTicket(idCounter--, "Archived support ticket #" + (i + 1),
                    CATEGORIES[(i + 3) % CATEGORIES.length], Ticketpriority.LOW, Ticketstatus.CLOSED,
                    requesters[i % requesters.length], "IT Support",
                    LocalDate.of(2026, 10, Math.max(1, 6 - i))));
        }
    }

    private Ticket createSeededTicket(
            int num,
            String title,
            String category,
            Ticketpriority priority,
            Ticketstatus status,
            String requester,
            String technician,
            LocalDate date) {
        String id = String.format("HD-%05d", Math.max(1, num));
        List<String> log = new ArrayList<>();
        log.add("09:00 AM — Ticket submitted");
        if (!"Unassigned".equalsIgnoreCase(technician)) {
            log.add("09:30 AM — Assigned to " + technician);
        }
        if (status == Ticketstatus.RESOLVED || status == Ticketstatus.CLOSED) {
            log.add("04:15 PM — Status updated to " + status.getDisplayName());
        }
        return new Ticket(
                id,
                title,
                "Support request logged under " + category + " category for " + title + ".",
                category,
                priority,
                status,
                requester,
                technician,
                "",
                date,
                log
        );
    }

    @Override
    public synchronized List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }

    @Override
    public synchronized List<Ticket> findRecent(int limit) {
        int count = Math.min(Math.max(0, limit), tickets.size());
        return new ArrayList<>(tickets.subList(0, count));
    }

    @Override
    public synchronized List<Ticket> findByRequester(String requester) {
        if (requester == null || requester.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String target = requester.trim();
        return tickets.stream()
                .filter(t -> target.equalsIgnoreCase(t.getRequester()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Ticket> findByDateRange(LocalDate fromDate, LocalDate toDate) {
        return tickets.stream()
                .filter(t -> {
                    LocalDate d = t.getCreatedDate();
                    if (d == null) {
                        return true;
                    }
                    boolean afterFrom = (fromDate == null) || !d.isBefore(fromDate);
                    boolean beforeTo = (toDate == null) || !d.isAfter(toDate);
                    return afterFrom && beforeTo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Optional<Ticket> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        String target = id.trim();
        return tickets.stream()
                .filter(t -> target.equalsIgnoreCase(t.getId()))
                .findFirst();
    }

    @Override
    public synchronized Ticket createTicket(
            String title,
            String description,
            String category,
            Ticketpriority priority,
            String requester,
            String attachmentPath) {
        String newId = String.format("HD-%05d", nextSequenceNumber++);
        List<String> initialActivity = new ArrayList<>();
        initialActivity.add(Ticket.formatNow() + " — Ticket submitted by "
                + (requester == null || requester.trim().isEmpty() ? DEMO_CURRENT_REQUESTER : requester.trim()));
        if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
            initialActivity.add(Ticket.formatNow() + " — Attachment added: " + attachmentPath.trim());
        }
        Ticket ticket = new Ticket(
                newId,
                title,
                description,
                category,
                priority,
                Ticketstatus.OPEN,
                requester,
                "Unassigned",
                attachmentPath,
                LocalDate.now(),
                initialActivity
        );
        tickets.add(0, ticket);
        return ticket;
    }

    @Override
    public synchronized boolean save(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return false;
        }
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equalsIgnoreCase(ticket.getId())) {
                tickets.set(i, ticket);
                return true;
            }
        }
        tickets.add(0, ticket);
        return true;
    }

    @Override
    public synchronized boolean updateTicket(Ticket ticket) {
        return save(ticket);
    }

    @Override
    public synchronized int countAll() {
        return tickets.size();
    }

    @Override
    public synchronized int countByStatus(Ticketstatus status) {
        if (status == null) {
            return 0;
        }
        return (int) tickets.stream()
                .filter(t -> t.getStatus() == status)
                .count();
    }
}