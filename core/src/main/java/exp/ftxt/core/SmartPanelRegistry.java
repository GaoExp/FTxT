package exp.ftxt.core;

import java.util.ArrayList;
import java.util.List;

import exp.ftxt.shared.ui.SmartPanelTarget;

public class SmartPanelRegistry {

    public static class Entry {
        public final String id;
        public final String title;
        public final TargetProvider targetProvider;
        public final Runnable enableAction;
        public final Runnable disableAction;

        public interface TargetProvider {
            SmartPanelTarget get();
        }

        public Entry(String id, String title, TargetProvider targetProvider,
                     Runnable enableAction, Runnable disableAction) {
            this.id = id;
            this.title = title;
            this.targetProvider = targetProvider;
            this.enableAction = enableAction;
            this.disableAction = disableAction;
        }
    }

    private static final List<Entry> entries = new ArrayList<>();

    public static void register(Entry entry) {
        entries.add(entry);
    }

    public static void clear() {
        entries.clear();
    }

    public static List<Entry> getEntries() {
        return entries;
    }

    public static Entry find(String id) {
        for (Entry e : entries) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }

    public static SmartPanelTarget getTarget(String id) {
        Entry e = find(id);
        return e != null ? e.targetProvider.get() : null;
    }
}
