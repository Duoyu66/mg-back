package com.example.mg.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryKVStore {
    private static class Entry {
        String val;
        long expireAt;
    }
    private final Map<String, Entry> map = new ConcurrentHashMap<>();
    public String get(String key) {
        Entry e = map.get(key);
        if (e == null) return null;
        if (e.expireAt > 0 && System.currentTimeMillis() > e.expireAt) {
            map.remove(key);
            return null;
        }
        return e.val;
    }
    public void set(String key, String val, long ttlSeconds) {
        Entry e = new Entry();
        e.val = val;
        e.expireAt = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000 : 0;
        map.put(key, e);
    }
    public Long increment(String key) {
        Entry e = map.get(key);
        long v = 0;
        if (e != null && e.val != null) {
            try { v = Long.parseLong(e.val); } catch (NumberFormatException ignored) {}
        }
        v++;
        set(key, Long.toString(v), 0);
        return v;
    }
    public void expire(String key, Duration d) {
        Entry e = map.get(key);
        if (e == null) return;
        e.expireAt = System.currentTimeMillis() + d.toMillis();
        map.put(key, e);
    }
    public void delete(String key) {
        map.remove(key);
    }
}
