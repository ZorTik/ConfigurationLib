package me.zort.configurationlib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Placeholders extends HashMap<String, Object> {

    public static Placeholders of(String key, Object value) {
        return new Placeholders().and(key, value);
    }

    public String replace(String s) {
        for(String placeholder : keySet()) {
            s = s.replaceAll(placeholder, getOrDefault(placeholder, "").toString());
        }
        return s;
    }

    public List<String> replace(Iterable<String> iter) {
        List<String> result = new ArrayList<>();
        for(String s : iter) {
            result.add(replace(s));
        }
        return result;
    }

    public Placeholders and(String placeholder, Object value) {
        put(placeholder, value);
        return this;
    }

    public Placeholders and(Map<String, Object> placeholders) {
        this.putAll(placeholders);
        return this;
    }

}
