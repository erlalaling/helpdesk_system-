
package helpdesksystemui;

import java.io.BufferedReader;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
        "createdDate", "activityLog"
    };

    // Reads/writes tickets.csv in the application's working directory.
    private final Path csvFile = Paths.get("tickets.csv").toAbsolutePath();

    private final List<Ticket> tickets = new ArrayList<>();
    private int nextSequenceNumber = 1;

    public Demoticketrepository() {
        loadTickets();
    }

    // -------------------- CSV READING --------------------

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
                tickets.add(ticket);

                String id = ticket.getId();
                if (id.matches("HD-\\d+")) {
                    int number = Integer.parseInt(id.substring(3));
                    maxId = Math.max(maxId, number);
                }
            }

            nextSequenceNumber = maxId + 1;

            System.out.println("Tickets loaded: " + tickets.size());
            System.out.println("CSV file: " + csvFile);

        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                "Could not load tickets from " + csvFile
                + ". Check the CSV file and its contents.", e
            );
        }
    }

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
            activity
        );
    }

    /*
     * Reads complete CSV records, including fields containing commas,
     * escaped quotes, or line breaks.
     */
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

    private void createEmptyCsv() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                csvFile, StandardCharsets.UTF_8)) {
            writeCsvRecord(writer, java.util.Arrays.asList(CSV_HEADER));
        }
    }

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
            String.join(" || ", ticket.getActivityLog())
        );
    }

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

    // -------------------- CREATE AND UPDATE --------------------

    @Override
    public synchronized Ticket createTicket(
            String title,
            String description,
            String category,
            Ticketpriority priority,
            String requester,
            String attachmentPath) {

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
            activity
        );

        tickets.add(0, ticket);

        if (!writeTicketsToCsv()) {
            tickets.remove(ticket);
            throw new IllegalStateException(
                "Ticket was not saved to the CSV file: " + csvFile
            );
        }

        nextSequenceNumber++;
        return ticket;
    }

    @Override
    public synchronized boolean save(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return false;
        }

        int existingIndex = -1;

        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equalsIgnoreCase(ticket.getId())) {
                existingIndex = i;
                break;
            }
        }

        Ticket previous = null;

        if (existingIndex >= 0) {
            previous = tickets.set(existingIndex, ticket);
        } else {
            tickets.add(0, ticket);
        }

        if (!writeTicketsToCsv()) {
            if (existingIndex >= 0) {
                tickets.set(existingIndex, previous);
            } else {
                tickets.remove(ticket);
            }

            return false;
        }

        return true;
    }

    @Override
    public synchronized boolean updateTicket(Ticket ticket) {
        return save(ticket);
    }

    // -------------------- COUNTS --------------------

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