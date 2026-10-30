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
    private int totalTime = 0;
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
            new ArrayList<>(), 0
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
            List<String> initialActivity, int totalTime) {
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
        this.totalTime = totalTime;
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

    // TIMEEEE 10:00AM = 600
    // totalTime is the minute of the day (0-1439) at which the ticket was created.
    // It is saved in the CSV "totalTime" column, so the creation hour survives a restart.
    public void setTimee(int totalTime){
        this.totalTime = totalTime;
    }

    public int getTime(){
        return totalTime;
    }

    // Creation time of day, rebuilt from the stored minute of the day.
    // Out-of-range values are clamped so a bad CSV value cannot crash the sorting.
    public LocalTime getCreatedTime() {
        int minutes = Math.max(0, Math.min(totalTime, 24 * 60 - 1));
        return LocalTime.of(minutes / 60, minutes % 60);
    }

    // Hour bucket (0-23) used to group tickets inside one date.
    public int getCreatedHour() {
        return getCreatedTime().getHour();
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

     /*
        Negative if the first object should go first in a list.
        Positive if the second object should go first in a list.
        Zero if the order does not matter.
    */


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