package com.mateolegi.despliegues;

import com.mateolegi.despliegues.process.AsyncManager;
import com.mateolegi.despliegues.process.Event;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Root {

    /** Event called after a promise finish */
    public static final String PROCESS_FINISHED = "Process finished";
    public static final String SUCCESS = "Success";
    public static final String ERROR = "Error";

    private static Root INSTANCE;
    private final MultiValuedMap<String, Consumer<Event>> events = new ArrayListValuedHashMap<>();
    private final Map<String, Function<Event, Event.Confirmation>> confirmationEvents = new HashMap<>();
    private AsyncManager manager;

    private Root() {}

    public static Root get() {
        if (INSTANCE == null) {
            synchronized (Root.class) {
                newRoot();
            }
        }
        return INSTANCE;
    }

    public static Root newRoot() {
        synchronized (Root.class) {
            INSTANCE = new Root();
        }
        return INSTANCE;
    }

    public Root on(String event, Consumer<Event> then) {
        events.put(event, then);
        return this;
    }

    public Root on(String event, Function<Event, Event.Confirmation> then) {
        confirmationEvents.put(event, then);
        return this;
    }

    public void emit(String event) {
        events.get(event).forEach(then -> then.accept(new Event()));
    }

    public void emit(String event, Event args) {
        events.get(event).forEach(then -> then.accept(args));
    }

    public void emit(String event, Map<String, Object> args) {
        Event.Builder builder = new Event.Builder();
        if (args != null) builder.withArgs(args);
        Event ev = builder.build();
        events.get(event).forEach(then -> then.accept(ev));
    }

    public Event.Confirmation emitConfirmation(String event) {
        return confirmationEvents.getOrDefault(event, __ -> Event.Confirmation.NOT_EXISTING_EVENT).apply(new Event());
    }

    public Event.Confirmation emitConfirmation(String event, Event args) {
        return confirmationEvents.getOrDefault(event, __ -> Event.Confirmation.NOT_EXISTING_EVENT).apply(args);
    }

    public AsyncManager withManager(AsyncManager manager) {
        this.manager = manager;
        return this.manager;
    }

    public AsyncManager manager() {
        this.manager = new AsyncManager();
        return this.manager;
    }
}
