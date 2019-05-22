package com.amazon.polly.demos.wordconverter.models;

import java.util.Map;

public class PollySSMLContext<K,V> {
    Map<K,V> context;
    int breakMillis;

    public PollySSMLContext(int breakMillis, Map<K, V> context) {
        this.context = context;
        this.breakMillis = breakMillis;
    }

    public PollySSMLContext() {
    }

    public Map<K, V> getContext() {
        return context;
    }

    public void setContext(Map<K, V> context) {
        this.context = context;
    }

    public int getBreakMillis() {
        return breakMillis;
    }

    public void setBreakMillis(int breakMillis) {
        this.breakMillis = breakMillis;
    }
}
