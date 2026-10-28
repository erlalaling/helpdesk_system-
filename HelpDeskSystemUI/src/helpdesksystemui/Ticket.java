package helpdesksystemui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ticket {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final String id;
    private String title;
    private String description;
    private String category;
    private Ticketpriority priority;
    private Ticketstatus status;
    private String requester;
    private String assignedTechnician;
    private String attachmentPath;
    private LocalDate createdDate;
    private final List<String> activityLog;

    public Ticket(String id, String subject, Ticketpriority priority, Ticketstatus status) {
        this(
            id,
            subject,
            "No additional description provided.",
            "General",
            priority,
            status,
            Demoticketrepository.DEMO_CURRENT_REQUESTER,
            "Unassigned",
            "",
            LocalDate.now(),
            new ArrayList<>()
        );
    }

    public Ticket(
            String id,
            String title,
            String description,
            String category,
            Ticketpriority priority,
            Ticketstatus status,
            String requester,
            String assignedTechnician,
            String attachmentPath,
            LocalDate createdDate,
            List<String> initialActivity) {
        this.id = Objects.requireNonNull(id, "Ticket ID must not be null");
        this.title = title == null ? "" : title.trim();
        this.description = description == null ? "" : description.trim();
        this.category = (category == null || category.trim().isEmpty()) ? "Hardware" : category.trim();
        this.priority = priority == null ? Ticketpriority.MEDIUM : priority;
        this.status = status == null ? Ticketstatus.OPEN : status;
        this.requester = (requester == null || requester.trim().isEmpty())
                ? Demoticketrepository.DEMO_CURRENT_REQUESTER
                : requester.trim();
        this.assignedTechnician = (assignedTechnician == null || assignedTechnician.trim().isEmpty())
                ? "Unassigned"
                : assignedTechnician.trim();
        this.attachmentPath = attachmentPath == null ? "" : attachmentPath.trim();
        this.createdDate = createdDate == null ? LocalDate.now() : createdDate;
        this.activityLog = new ArrayList<>();
        if (initialActivity != null && !initialActivity.isEmpty()) {
            this.activityLog.addAll(initialActivity);
        } else {
            this.activityLog.add(formatNow() + " — Ticket submitted");
        }
    }

    public static String formatNow() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title.trim();
    }

    public void setSubject(String subject) {
        setTitle(subject);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? "Hardware" : category.trim();
    }

    public Ticketpriority getPriority() {
        return priority;
    }

    public void setPriority(Ticketpriority priority) {
        if (priority != null) {
            this.priority = priority;
        }
    }

    public Ticketstatus getStatus() {
        return status;
    }

    public void setStatus(Ticketstatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        if (requester != null && !requester.trim().isEmpty()) {
            this.requester = requester.trim();
        }
    }

    public String getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(String assignedTechnician) {
        this.assignedTechnician = (assignedTechnician == null || assignedTechnician.trim().isEmpty())
                ? "Unassigned"
                : assignedTechnician.trim();
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath == null ? "" : attachmentPath.trim();
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        if (createdDate != null) {
            this.createdDate = createdDate;
        }
    }

    public List<String> getActivityLog() {
        return Collections.unmodifiableList(activityLog);
    }

    public void addActivity(String message) {
        if (message != null && !message.trim().isEmpty()) {
            activityLog.add(formatNow() + " — " + message.trim());
        }
    }

    public void addRawActivity(String entry) {
        if (entry != null && !entry.trim().isEmpty()) {
            activityLog.add(entry.trim());
        }
    }
}