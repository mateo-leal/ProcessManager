package com.mateolegi.despliegues.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Event {

    private Map<String, Object> args = new HashMap<>();

    public Event() { }

    public Event(Map<String, Object> args) {
        this.args = args;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
    public Object getArg(String name) {
        return Optional.ofNullable(this.args.get(name));
    }

    public static class Builder {
        private Map<String, Object> args = new HashMap<>();

        public Builder withArgs(Map<String, Object> args) {
            this.args.putAll(args);
            return this;
        }
        public Builder withArg(String name, Object arg) {
            this.args.put(name, arg);
            return this;
        }
        public Event build() {
            return new Event(args);
        }
    }
}
