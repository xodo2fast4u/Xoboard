package com.xodobyte.xoboard;

import android.content.Context;
import android.content.SharedPreferences;

final class XoPrefs {

    static final String FILE = "xoboard";
    static final String KEY_THEME = "theme";
    static final String KEY_LIMIT = "clip_limit";
    static final String KEY_CLIPS = "clips";
    static final String KEY_CUSTOM_BG = "custom_bg";
    static final String KEY_CUSTOM_KEY = "custom_key";
    static final String KEY_CUSTOM_TEXT = "custom_text";
    static final String KEY_CUSTOM_ACCENT = "custom_accent";
    static final String KEY_HAPTIC = "haptic";
    static final String KEY_AUTOCAP = "autocap";
    static final String KEY_DOUBLE_SPACE = "double_space";
    static final String KEY_AUTO_TEXT = "auto_text";
    static final String KEY_AUTO_SPEED = "auto_speed";
    static final String KEY_AUTO_SEND = "auto_send";
    static final String KEY_AUTO_PAUSE = "auto_pause";
    static final String KEY_AUTO_REPEAT = "auto_repeat";
    static final String KEY_ACTIVE_STYLE = "active_style";
    static final String KEY_RECENT_EMOJI = "recent_emoji";

    static final String THEME_SYSTEM = "system";
    static final String THEME_LIGHT = "light";
    static final String THEME_DARK = "dark";
    static final String THEME_CUSTOM = "custom";

    private XoPrefs() {
    }

    static SharedPreferences get(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
