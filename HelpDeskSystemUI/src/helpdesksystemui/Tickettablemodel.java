package helpdesksystemui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class Tickettablemodel extends AbstractTableModel {

    public enum Mode {
        DASHBOARD(new String[]{"TICKET ID", "SUBJECT", "PRIORITY", "STATUS", "ACTION"}),
        MY_TICKETS(new String[]{"Ticket ID", "Title", "Category", "Priority", "Status", "Action"}),
        ALL_TICKETS(new String[]{"Ticket ID", "Requester", "Title", "Priority", "Status", "Action"});

        private final String[] columns;

        Mode(String[] columns) {
            this.columns = columns;
        }

        public String[] getColumns() {
            return columns;
        }
    }

    private final Mode mode;
    private final List<Ticket> tickets = new ArrayList<>();

    public Tickettablemodel() {
        this(Mode.DASHBOARD);
    }

    public Tickettablemodel(Mode mode) {
        this.mode = mode == null ? Mode.DASHBOARD : mode;
    }

    public void setTickets(List<Ticket> newTickets) {
        tickets.clear();
        if (newTickets != null) {
            tickets.addAll(newTickets);
        }
        fireTableDataChanged();
    }

    public Ticket getTicketAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= tickets.size()) {
            return null;
        }
        return tickets.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return tickets.size();
    }

    @Override
    public int getColumnCount() {
        return mode.getColumns().length;
    }

    @Override
    public String getColumnName(int column) {
        return mode.getColumns()[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Ticket ticket = getTicketAt(rowIndex);
        if (ticket == null) {
            return "";
        }
        switch (mode) {
            case MY_TICKETS:
                switch (columnIndex) {
                    case 0:
                        return ticket.getId();
                    case 1:
                        return ticket.getTitle();
                    case 2:
                        return ticket.getCategory();
                    case 3:
                        return ticket.getPriority();
                    case 4:
                        return ticket.getStatus();
                    case 5:
                        return "View";
                    default:
                        return "";
                }
            case ALL_TICKETS:
                switch (columnIndex) {
                    case 0:
                        return ticket.getId();
                    case 1:
                        return ticket.getRequester();
                    case 2:
                        return ticket.getTitle();
                    case 3:
                        return ticket.getPriority();
                    case 4:
                        return ticket.getStatus();
                    case 5:
                        return "View";
                    default:
                        return "";
                }
            case DASHBOARD:
            default:
                switch (columnIndex) {
                    case 0:
                        return ticket.getId();
                    case 1:
                        return ticket.getSubject();
                    case 2:
                        return ticket.getPriority();
                    case 3:
                        return ticket.getStatus();
                    case 4:
                        return "View";
                    default:
                        return "";
                }
        }
    }
}