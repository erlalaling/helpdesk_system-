package helpdesksystemui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Demoticketrepository implements Ticketrepository {

    public static final String DEMO_CURRENT_REQUESTER = "Sean Gerolaga";

    public static final String[] CATEGORIES = {
        "Hardware", "Network", "Account", "Software", "Facilities"
    };

    public static final String[] TECHNICIANS = {
        "Unassigned", "IT Support", "Mark Villanueva",
        "Jessa Mae Cruz", "Ryan Dela Cruz"
    };

    private static final String[] CSV_HEADER = {
        "id", "title", "description", "category", "priority",
        "status", "requester", "technician", "attachmentPath",
        "createdDate", "activityLog", "totalTime"
    };

    // Reads/writes tickets.csv in the application's working directory.
    private final Path csvFile = Paths.get("tickets.csv").toAbsolutePath();

    private final Queue<Ticket> tickets = new ArrayDeque<>();
    private int nextSequenceNumber = 1;

    // Creates the repository and loads saved tickets from the CSV file.
    public Demoticketrepository() {
        loadTickets();
    }

    // -------------------- SORTED COLLECTIONS --------------------
    //
    //   tickets           Queue (ArrayDeque)  every ticket, in CSV / arrival order.
    //                     This is the source of truth and what the list screens read.
    //
    //   groupByDate       TreeMap -> Hashtable -> PriorityQueue
    //                     the UNFINISHED tickets (OPEN, IN_PROGRESS) sorted as:
    //                       1. oldest date first        (TreeMap key order)
    //                       2. earliest hour first      (Hashtable key = hour 0-23)
    //                       3. most urgent first        (PriorityQueue + comparator)
    //
    //   completedTickets  Queue (ArrayDeque)  RESOLVED and CLOSED tickets.
    //
    // groupByDate and completedTickets are indexes built FROM `tickets`.
    // They are never written to the CSV, so loading the CSV rebuilds them.
    // Every ticket is in exactly one of the two.

    /*
        Negative if the first object should go first in a list.
        Positive if the second object should go first in a list.
        Zero if the order does not matter.

        Ticketpriority declares URGENT first, so URGENT sorts before LOW.
        getTime() is the minute of the day the ticket was created.
        The id is the last tie-breaker so the order is always the same.
    */
    private static final Comparator<Ticket> ORDERbyPRIORITYthenDATE = Comparator.comparing((Ticket ticket) -> ticket.getPriority())
        .thenComparingInt(ticket -> ticket.getTime())
        .thenComparing(ticket -> ticket.getCreatedDate())
        .thenComparing(ticket -> ticket.getId());

    private final TreeMap<LocalDate, Hashtable<Integer, PriorityQueue<Ticket>>> groupByDate = new TreeMap<>();

    private final Queue<Ticket> completedTickets = new ArrayDeque<>();

    // True for tickets that need no more work.
    private static boolean isCompleted(Ticket t) {
        return t.getStatus() == Ticketstatus.RESOLVED || t.getStatus() == Ticketstatus.CLOSED;
    }

    // Files one ticket into the sorted structure, or into the completed collection.
    private void submit(Ticket t) {
        if (isCompleted(t)) {
            completedTickets.add(t);
            return;
        }

        groupByDate
            .computeIfAbsent(t.getCreatedDate(), d -> new Hashtable<>())
            .computeIfAbsent(t.getCreatedHour(), h -> new PriorityQueue<>(ORDERbyPRIORITYthenDATE))
            .offer(t);
    }

    // Throws both indexes away and rebuilds them from the master queue.
    // Needed after an edit because changing a priority or status while a ticket
    // sits inside a PriorityQueue leaves that queue in the wrong order.
    private void rebuildIndexes() {
        groupByDate.clear();
        completedTickets.clear();

        for (Ticket t : tickets) {
            submit(t);
        }
    }

    // Returns the unfinished tickets in processing order WITHOUT changing the real queues.
    private List<Ticket> activeTicketsInOrder() {
        List<Ticket> ordered = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if (!isCompleted(ticket)) {
                ordered.add(ticket);
            }
        }

        insertionSort(ordered);
        return ordered;
    }

    // Sorts tickets from oldest and earliest to most urgent using insertion sort.
    private void insertionSort(List<Ticket> ticketsToSort) {
        for (int i = 1; i < ticketsToSort.size(); i++) {
            Ticket current = ticketsToSort.get(i);
            int position = i - 1;

            while (position >= 0
                    && compareForProcessing(ticketsToSort.get(position), current) > 0) {
                ticketsToSort.set(position + 1, ticketsToSort.get(position));
                position--;
            }

            ticketsToSort.set(position + 1, current);
        }
    }

    private int compareForProcessing(Ticket first, Ticket second) {
        int comparison = first.getCreatedDate().compareTo(second.getCreatedDate());
        if (comparison != 0) {
            return comparison;
        }

        comparison = Integer.compare(first.getCreatedHour(), second.getCreatedHour());
        if (comparison != 0) {
            return comparison;
        }

        comparison = first.getPriority().compareTo(second.getPriority());
        if (comparison != 0) {
            return comparison;
        }

        comparison = Integer.compare(first.getTime(), second.getTime());
        if (comparison != 0) {
            return comparison;
        }

        return first.getId().compareTo(second.getId());
    }

    // Takes the next OPEN ticket in processing order and marks it IN_PROGRESS.
    // Returns null if there is no OPEN ticket or the change could not be saved.
    public synchronized Ticket nextTicket() {
        for (Ticket candidate : activeTicketsInOrder()) {
            if (candidate.getStatus() != Ticketstatus.OPEN) {
                continue;
            }

            candidate.setStatus(Ticketstatus.IN_PROGRESS);
            candidate.addActivity("Status changed to In Progress (next in queue)");

            if (!save(candidate)) {
                candidate.setStatus(Ticketstatus.OPEN);
                return null;
            }
            return candidate;
        }

        return null;
    }



    // -------------------- CSV READING --------------------

    // Loads ticket records from the CSV file into the in-memory queue.
    private void loadTickets() {
        try {
            if (!Files.exists(csvFile)) {
                createEmptyCsv();
                return;
            }

            List<List<String>> records = readCsvRecords();

            if (records.isEmpty()) {
                createEmptyCsv();
                return;
            }

            // The first record contains the column headers.
            int maxId = 0;

            for (int i = 1; i < records.size(); i++) {
                List<String> row = records.get(i);

                if (row.size() == 1 && row.get(0).trim().isEmpty()) {
                    continue;
                }

                if (row.size() != CSV_HEADER.length) {
                    throw new IOException(
                        "Invalid CSV row " + (i + 1)
                        + ": expected " + CSV_HEADER.length
                        + " columns but found " + row.size()
                    );
                }

                Ticket ticket = rowToTicket(row);
                tickets.add(ticket);   // master queue keeps CSV order
                submit(ticket);        // sorted structure / completed collection

                String id = ticket.getId();
                if (id.matches("HD-\\d+")) {
                    int number = Integer.parseInt(id.substring(3));
                    maxId = Math.max(maxId, number);
                }
            }

            nextSequenceNumber = maxId + 1;

            System.out.println("Tickets loaded: " + tickets.size()
                + " (unfinished: " + (tickets.size() - completedTickets.size())
                + ", completed: " + completedTickets.size() + ")");
            System.out.println("CSV file: " + csvFile);

        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                "Could not load tickets from " + csvFile
                + ". Check the CSV file and its contents.", e
            );
        }
    }

    // Converts one CSV row into a Ticket object.
    private Ticket rowToTicket(List<String> row) {
        String activityField = row.get(10);

        List<String> activity = new ArrayList<>();

        if (!activityField.isEmpty()) {
            Collections.addAll(activity, activityField.split(" \\|\\| ", -1));
        }

        return new Ticket(
            row.get(0),
            row.get(1),
            row.get(2),
            row.get(3),
            Ticketpriority.valueOf(row.get(4).trim().toUpperCase(Locale.ROOT)),
            Ticketstatus.valueOf(row.get(5).trim().toUpperCase(Locale.ROOT)),
            row.get(6),
            row.get(7),
            row.get(8),
            LocalDate.parse(row.get(9)),
            activity,
            Integer.parseInt(row.get(11).trim())
        );
    }

    /*
     * Reads complete CSV records, including fields containing commas,
     * escaped quotes, or line breaks.
     */
    // Parses the CSV file into rows while supporting quoted fields.
    private List<List<String>> readCsvRecords() throws IOException {
        List<List<String>> records = new ArrayList<>();

        try (PushbackReader reader = new PushbackReader(
                Files.newBufferedReader(csvFile, StandardCharsets.UTF_8), 1)) {

            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            boolean recordStarted = false;
            int value;

            while ((value = reader.read()) != -1) {
                char c = (char) value;

                if (inQuotes) {
                    if (c == '"') {
                        int next = reader.read();

                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            if (next != -1) {
                                reader.unread(next);
                            }
                        }
                    } else {
                        field.append(c);
                    }

                    continue;
                }

                if (c == '"' && field.length() == 0) {
                    inQuotes = true;
                    recordStarted = true;
                } else if (c == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    recordStarted = true;
                } else if (c == '\n' || c == '\r') {
                    if (c == '\r') {
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            reader.unread(next);
                        }
                    }

                    row.add(field.toString());
                    records.add(row);

                    row = new ArrayList<>();
                    field.setLength(0);
                    recordStarted = false;
                } else {
                    field.append(c);
                    recordStarted = true;
                }
            }

            if (inQuotes) {
                throw new IOException("Unclosed quotation mark in CSV.");
            }

            if (recordStarted || !row.isEmpty() || field.length() > 0) {
                row.add(field.toString());
                records.add(row);
            }
        }

        return records;
    }

    // -------------------- CSV WRITING --------------------

    // Creates a new CSV file containing only the ticket headers.
    private void createEmptyCsv() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                csvFile, StandardCharsets.UTF_8)) {
            writeCsvRecord(writer, java.util.Arrays.asList(CSV_HEADER));
        }
    }

    // Writes all queued tickets to the CSV file using a temporary file.
    private boolean writeTicketsToCsv() {
        Path tempFile = null;

        try {
            Path parent = csvFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            tempFile = Files.createTempFile(
                parent, "tickets-", ".tmp"
            );

            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempFile, StandardCharsets.UTF_8)) {

                writeCsvRecord(writer, java.util.Arrays.asList(CSV_HEADER));

                for (Ticket ticket : tickets) {
                    writeCsvRecord(writer, ticketToRow(ticket));
                }
            }

            try {
                Files.move(
                    tempFile,
                    csvFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                    tempFile,
                    csvFile,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

            return true;

        } catch (IOException e) {
            System.err.println("Failed to save tickets to " + csvFile);
            e.printStackTrace();
            return false;

        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    System.err.println("Could not delete temporary CSV file.");
                }
            }
        }
    }

    // Converts a Ticket object into a CSV row.
    private List<String> ticketToRow(Ticket ticket) {
        return java.util.Arrays.asList(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCategory(),
            ticket.getPriority().name(),
            ticket.getStatus().name(),
            ticket.getRequester(),
            ticket.getAssignedTechnician(),
            ticket.getAttachmentPath(),
            ticket.getCreatedDate().toString(),
            String.join(" || ", ticket.getActivityLog()),
            String.valueOf(ticket.getTime())
        );
    }

    // Writes one CSV row and escapes fields containing special characters.
    private void writeCsvRecord(
            BufferedWriter writer, List<String> fields) throws IOException {

        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                writer.write(',');
            }

            String value = fields.get(i) == null ? "" : fields.get(i);

            // Quote fields that contain CSV-special characters.
            if (value.contains(",") || value.contains("\"")
                    || value.contains("\n") || value.contains("\r")) {

                writer.write('"');
                writer.write(value.replace("\"", "\"\""));
                writer.write('"');

            } else {
                writer.write(value);
            }
        }

        writer.newLine();
    }

    // -------------------- SEARCH METHODS --------------------

    // Returns a list copy containing every stored ticket.
    @Override
    public synchronized List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }

    // Returns the last requested number of tickets in queue order.
    @Override
    public synchronized List<Ticket> findRecent(int limit) {
        int count = Math.min(Math.max(0, limit), tickets.size());
        int ticketsToSkip = tickets.size() - count;
        List<Ticket> recentTickets = new ArrayList<>(count);
        int index = 0;

        for (Ticket ticket : tickets) {
            if (index++ >= ticketsToSkip) {
                recentTickets.add(ticket);
            }
        }

        return recentTickets;
    }

    // Returns up to `limit` unfinished tickets in processing order:
    // oldest date, earliest hour, then most urgent priority.
    @Override
    public synchronized List<Ticket> findPrioritized(int limit) {
        List<Ticket> ordered = activeTicketsInOrder();
        int count = Math.min(Math.max(0, limit), ordered.size());
        return new ArrayList<>(ordered.subList(0, count));
    }

    // Returns a copy of the RESOLVED and CLOSED tickets.
    @Override
    public synchronized List<Ticket> findCompleted() {
        return new ArrayList<>(completedTickets);
    }

    // Returns tickets submitted by the specified requester.
    @Override
    public synchronized List<Ticket> findByRequester(String requester) {
        if (requester == null || requester.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String target = requester.trim();
        List<Ticket> matches = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if (target.equalsIgnoreCase(ticket.getRequester())) {
                matches.add(ticket);
            }
        }

        return matches;
    }

    // Returns tickets whose creation dates fall within the specified range.
    @Override
    public synchronized List<Ticket> findByDateRange(
            LocalDate fromDate, LocalDate toDate) {

        return tickets.stream()
            .filter(t -> {
                LocalDate date = t.getCreatedDate();

                boolean afterFrom =
                    fromDate == null || !date.isBefore(fromDate);
                boolean beforeTo =
                    toDate == null || !date.isAfter(toDate);

                return afterFrom && beforeTo;
            })
            .collect(Collectors.toList());
    }

    // Finds and returns a ticket by its ID.
    @Override
    public synchronized Optional<Ticket> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String target = id.trim();

        for (Ticket ticket : tickets) {
            if (target.equalsIgnoreCase(ticket.getId())) {
                return Optional.of(ticket);
            }
        }

        return Optional.empty();
    }

    // -------------------- CREATE AND UPDATE --------------------

    // Creates, stores, and persists a new open ticket.
    @Override
    public synchronized Ticket createTicket(
            String title,
            String description,
            String category,
            Ticketpriority priority,
            String requester,
            String attachmentPath, int totalTime) {

        String newId = String.format("HD-%05d", nextSequenceNumber);

        List<String> activity = new ArrayList<>();

        activity.add(
            Ticket.formatNow() + " — Ticket submitted by "
            + (requester == null || requester.trim().isEmpty()
                ? DEMO_CURRENT_REQUESTER : requester.trim())
        );

        if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
            activity.add(
                Ticket.formatNow() + " — Attachment added: "
                + attachmentPath.trim()
            );
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
            activity, totalTime
        );

        tickets.add(ticket);

        if (!writeTicketsToCsv()) {
            tickets.remove(ticket);
            throw new IllegalStateException(
                "Ticket was not saved to the CSV file: " + csvFile
            );
        }

        submit(ticket);   // new tickets are OPEN, so it goes into the sorted structure
        nextSequenceNumber++;
        return ticket;
    }

    // Adds or replaces a ticket and persists the updated queue.
    @Override
    public synchronized boolean save(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return false;
        }

        Queue<Ticket> previousTickets = new ArrayDeque<>(tickets);
        Queue<Ticket> updatedTickets = new ArrayDeque<>();
        boolean replaced = false;

        for (Ticket existingTicket : tickets) {
            if (!replaced && existingTicket.getId().equalsIgnoreCase(ticket.getId())) {
                updatedTickets.add(ticket);
                replaced = true;
            } else {
                updatedTickets.add(existingTicket);
            }
        }

        if (!replaced) {
            updatedTickets.add(ticket);
        }

        tickets.clear();
        tickets.addAll(updatedTickets);

        boolean written = writeTicketsToCsv();

        if (!written) {
            tickets.clear();
            tickets.addAll(previousTickets);
        }

        // Priority and status may have been edited in place on the ticket object,
        // so the sorted structure and completed collection are rebuilt either way.
        rebuildIndexes();

        return written;
    }

    // Updates an existing ticket by delegating to save.
    @Override
    public synchronized boolean updateTicket(Ticket ticket) {
        return save(ticket);
    }

    // -------------------- COUNTS --------------------

    // Returns the total number of stored tickets.
    @Override
    public synchronized int countAll() {
        return tickets.size();
    }

    // Counts tickets that have the specified status.
    @Override
    public synchronized int countByStatus(Ticketstatus status) {
        if (status == null) {
            return 0;
        }

        Queue<Ticket> source =
            (status == Ticketstatus.RESOLVED || status == Ticketstatus.CLOSED)
                ? completedTickets
                : tickets;

        return (int) source.stream()
            .filter(t -> t.getStatus() == status)
            .count();
    }
}
