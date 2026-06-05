package interfaces;

import model.LogEntry;

public interface LogListener {
    void onLogEntryCreated(LogEntry logEntry);
}
