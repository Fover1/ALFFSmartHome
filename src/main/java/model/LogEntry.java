package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LogEntry(

        LocalDateTime timeStamp,
        String scenarioName,
        String deviceName,
        String action,
        String result
) {
    /// todo: wie wollen wir das hier machen? wollen wir noch weitere Dinge loggen. Wenn ja, wie wollen wir das machen

    public LogEntry(String scenarioName, String deviceName, String action, String result) {
        this(LocalDateTime.now(), scenarioName, deviceName, action, result);
        System.out.println(this);
    }


    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] Szenario: '%s' | Gerät: '%s' | Aktion: '%s' -> %s",
                timeStamp.format(formatter), scenarioName, deviceName, action, result);
    }
}
