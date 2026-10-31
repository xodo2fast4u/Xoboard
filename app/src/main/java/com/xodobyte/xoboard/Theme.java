package com.xodobyte.xoboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

final class Theme {

    static final Theme LIGHT = new Theme(0xFFE7EBF0, 0xFFFFFFFF, 0xFFD2D8E0, 0xFF1C1F24, 0xFF3B82F6);
    static final Theme DARK = new Theme(0xFF15171C, 0xFF2A2E37, 0xFF1F2229, 0xFFF2F4F7, 0xFF60A5FA);

    final int background;
    final int key;
    final int special;
    final int text;
    final int accent;

    private Theme(int background, int key, int special, int text, int accent) {
        this.background = background;
        this.key = key;
        this.special = special;
        this.text = text;
        this.accent = accent;
    }

    static Theme resolve(Context context) {
        SharedPreferences p = XoPrefs.get(context);
        String mode = p.getString(XoPrefs.KEY_THEME, XoPrefs.THEME_SYSTEM);
        if (XoPrefs.THEME_CUSTOM.equals(mode)) {
            return custom(p);
        }
        if (XoPrefs.THEME_LIGHT.equals(mode)) {
            return LIGHT;
        }
        if (XoPrefs.THEME_DARK.equals(mode)) {
            return DARK;
        }
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES ? DARK : LIGHT;
    }

    private static Theme custom(SharedPreferences p) {
        int bg = color(p, XoPrefs.KEY_CUSTOM_BG, "#15171C");
        int key = color(p, XoPrefs.KEY_CUSTOM_KEY, "#2A2E37");
        int text = color(p, XoPrefs.KEY_CUSTOM_TEXT, "#F2F4F7");
        int accent = color(p, XoPrefs.KEY_CUSTOM_ACCENT, "#60A5FA");
        return new Theme(bg, key, mix(key, bg, 0.3f), text, accent);
    }

    static int color(SharedPreferences p, String key, String fallback) {
        try {
            return Color.parseColor(p.getString(key, fallback));
        } catch (IllegalArgumentException e) {
            return Color.parseColor(fallback);
        }
    }

    private static int mix(int a, int b, float t) {
        return Color.rgb(
                Math.round(Color.red(a) * (1 - t) + Color.red(b) * t),
                Math.round(Color.green(a) * (1 - t) + Color.green(b) * t),
                Math.round(Color.blue(a) * (1 - t) + Color.blue(b) * t));
    }
}
