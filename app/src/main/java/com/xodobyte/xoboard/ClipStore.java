package com.xodobyte.xoboard;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

final class ClipStore {

    private static final int MAX_ITEM_CHARS = 4000;

    private ClipStore() {
    }

    static int limit(Context context) {
        return XoPrefs.get(context).getInt(XoPrefs.KEY_LIMIT, 0);
    }

    static List<String> load(Context context) {
        List<String> out = new ArrayList<>();
        String raw = XoPrefs.get(context).getString(XoPrefs.KEY_CLIPS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                out.add(array.getString(i));
            }
        } catch (JSONException ignored) {
            out.clear();
        }
        return out;
    }

    static void save(Context context, List<String> items) {
        JSONArray array = new JSONArray();
        for (String item : items) {
            array.put(item);
        }
        XoPrefs.get(context).edit().putString(XoPrefs.KEY_CLIPS, array.toString()).commit();
    }

    static void add(Context context, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (text.length() > MAX_ITEM_CHARS) {
            text = text.substring(0, MAX_ITEM_CHARS);
        }
        List<String> items = load(context);
        items.remove(text);
        items.add(0, text);
        trim(items, limit(context));
        save(context, items);
    }

    static void remove(Context context, int index) {
        List<String> items = load(context);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            save(context, items);
        }
    }

    static void clear(Context context) {
        save(context, new ArrayList<String>());
    }

    static void enforceLimit(Context context) {
        List<String> items = load(context);
        trim(items, limit(context));
        save(context, items);
    }

    private static void trim(List<String> items, int limit) {
        if (limit <= 0) {
            return;
        }
        while (items.size() > limit) {
            items.remove(items.size() - 1);
        }
    }
}
