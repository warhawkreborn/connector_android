package it.thalhammer.warhawkreborn;

import java.util.*;

public class AppLog {
    public interface OnLogListener {
        void onLogUpdated(List<String> entries);
    }

    private static final AppLog instance = new AppLog();
    private List<String> log = new ArrayList<>();
    private Set<OnLogListener> listeners = new HashSet<>();

    public void addListener(OnLogListener l) {
        listeners.add(l);
    }

    public void removeListener(OnLogListener l) {
        listeners.remove(l);
    }

    public void addEntry(String entry) {
        log.add(entry);
        List<String> entries = getEntries();
        for(OnLogListener l : listeners) {
            l.onLogUpdated(entries);
        }
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(log);
    }

    public static AppLog getInstance() {
        return instance;
    }
}
