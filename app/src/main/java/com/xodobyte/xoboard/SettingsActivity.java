package com.xodobyte.xoboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class SettingsActivity extends Activity {

    private int bgColor;
    private int cardColor;
    private int surfaceColor;
    private int textColor;
    private int mutedColor;
    private int accentColor;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        applyPalette();
        getWindow().setStatusBarColor(bgColor);
        SharedPreferences p = XoPrefs.get(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(16), dp(24), dp(16), dp(24));
        scroll.addView(col);
        setContentView(scroll);

        col.addView(text("Xoboard", 26, textColor, true));
        col.addView(text("Keyboard settings", 14, mutedColor, false));

        LinearLayout setup = card(col, "Setup");
        pill(setup, "Enable keyboard", true, v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        pill(setup, "Choose active keyboard", false, v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });

        LinearLayout themeCard = card(col, "Theme");
        String[] modes = {XoPrefs.THEME_SYSTEM, XoPrefs.THEME_LIGHT, XoPrefs.THEME_DARK, XoPrefs.THEME_CUSTOM};
        String current = p.getString(XoPrefs.KEY_THEME, XoPrefs.THEME_SYSTEM);
        int selected = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(current)) {
                selected = i;
            }
        }
        segmented(themeCard, new String[]{"System", "Light", "Dark", "Custom"}, selected,
                i -> p.edit().putString(XoPrefs.KEY_THEME, modes[i]).apply());

        LinearLayout colorCard = card(col, "Custom colors");
        EditText bg = field(colorCard, "Background", p.getString(XoPrefs.KEY_CUSTOM_BG, "#15171C"), false);
        EditText keys = field(colorCard, "Keys", p.getString(XoPrefs.KEY_CUSTOM_KEY, "#2A2E37"), false);
        EditText txt = field(colorCard, "Text", p.getString(XoPrefs.KEY_CUSTOM_TEXT, "#F2F4F7"), false);
        EditText accent = field(colorCard, "Accent", p.getString(XoPrefs.KEY_CUSTOM_ACCENT, "#60A5FA"), false);
        pill(colorCard, "Save colors", true, v -> {
            String b = bg.getText().toString().trim();
            String k = keys.getText().toString().trim();
            String t = txt.getText().toString().trim();
            String a = accent.getText().toString().trim();
            if (!isColor(b) || !isColor(k) || !isColor(t) || !isColor(a)) {
                toast("Invalid color, use the form #RRGGBB");
                return;
            }
            p.edit()
                    .putString(XoPrefs.KEY_CUSTOM_BG, b)
                    .putString(XoPrefs.KEY_CUSTOM_KEY, k)
                    .putString(XoPrefs.KEY_CUSTOM_TEXT, t)
                    .putString(XoPrefs.KEY_CUSTOM_ACCENT, a)
                    .apply();
            toast("Colors saved");
        });

        LinearLayout clipCard = card(col, "Clipboard history (0 means unlimited)");
        EditText limit = field(clipCard, "Items to keep",
                String.valueOf(p.getInt(XoPrefs.KEY_LIMIT, 0)), false);
        limit.setInputType(InputType.TYPE_CLASS_NUMBER);
        pill(clipCard, "Save limit", true, v -> {
            int n;
            try {
                n = Integer.parseInt(limit.getText().toString().trim());
            } catch (NumberFormatException e) {
                n = -1;
            }
            if (n < 0) {
                toast("Enter a whole number, 0 or more");
                return;
            }
            p.edit().putInt(XoPrefs.KEY_LIMIT, n).apply();
            ClipStore.enforceLimit(this);
            toast("Limit saved");
        });
        pill(clipCard, "Clear clipboard history", false, v -> {
            ClipStore.clear(this);
            toast("Clipboard history cleared");
        });

        LinearLayout behavior = card(col, "Behavior");
        check(behavior, "Haptic feedback on keys", XoPrefs.KEY_HAPTIC, true);
        check(behavior, "Auto capitalize after sentences", XoPrefs.KEY_AUTOCAP, true);
        check(behavior, "Double space inserts a period", XoPrefs.KEY_DOUBLE_SPACE, true);
    }

    private int indexOf(int[] values, int target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == target) {
                return i;
            }
        }
        return 0;
    }

    private void applyPalette() {
        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (night) {
            bgColor = 0xFF0F1115;
            cardColor = 0xFF1B1E25;
            surfaceColor = 0xFF262A33;
            textColor = 0xFFF2F4F7;
            mutedColor = 0xFF9AA3B2;
            accentColor = 0xFF60A5FA;
        } else {
            bgColor = 0xFFF2F4F8;
            cardColor = 0xFFFFFFFF;
            surfaceColor = 0xFFE9EDF3;
            textColor = 0xFF1C1F24;
            mutedColor = 0xFF6B7280;
            accentColor = 0xFF3B82F6;
        }
    }

    private LinearLayout card(LinearLayout parent, String title) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(16));
        card.setBackground(rounded(cardColor, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        card.addView(text(title, 13, mutedColor, true));
        parent.addView(card);
        return card;
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        if (bold) {
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    private void text(LinearLayout parent, String value, int sizeSp, int color, boolean bold) {
        parent.addView(text(value, sizeSp, color, bold));
    }

    private void pill(LinearLayout card, String label, boolean primary, View.OnClickListener listener) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(15f);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        b.setTextColor(primary ? 0xFFFFFFFF : textColor);
        b.setBackground(rounded(primary ? accentColor : surfaceColor, 24));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(12), 0, 0);
        b.setLayoutParams(params);
        b.setOnClickListener(listener);
        card.addView(b);
    }

    private void segmented(LinearLayout card, String[] labels, int selected, IntConsumer onPick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dp(12), 0, 0);
        row.setLayoutParams(rowParams);
        List<TextView> items = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView t = new TextView(this);
            t.setText(labels[i]);
            t.setTextSize(14f);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(10), dp(4), dp(10));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(2), 0, dp(2), 0);
            t.setLayoutParams(lp);
            items.add(t);
            row.addView(t);
            t.setOnClickListener(v -> {
                for (int j = 0; j < items.size(); j++) {
                    highlight(items.get(j), j == index);
                }
                onPick.accept(index);
            });
            highlight(t, i == selected);
        }
        card.addView(row);
    }

    private void highlight(TextView t, boolean on) {
        t.setBackground(rounded(on ? accentColor : surfaceColor, 20));
        t.setTextColor(on ? 0xFFFFFFFF : textColor);
    }

    private EditText field(LinearLayout card, String label, String value, boolean multiline) {
        card.addView(text(label, 12, mutedColor, false));
        EditText e = new EditText(this);
        e.setText(value);
        e.setTextColor(textColor);
        e.setTextSize(15f);
        e.setTypeface(Typeface.DEFAULT);
        e.setHintTextColor(mutedColor);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackground(rounded(surfaceColor, 12));
        if (multiline) {
            e.setMinLines(4);
            e.setGravity(Gravity.TOP | Gravity.START);
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            e.setSingleLine(true);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(6), 0, 0);
        e.setLayoutParams(params);
        card.addView(e);
        return e;
    }

    private void check(LinearLayout card, String label, String key, boolean fallback) {
        SharedPreferences p = XoPrefs.get(this);
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setTextColor(textColor);
        box.setButtonTintList(ColorStateList.valueOf(accentColor));
        box.setChecked(p.getBoolean(key, fallback));
        box.setOnCheckedChangeListener((b, on) -> p.edit().putBoolean(key, on).apply());
        card.addView(box);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private boolean isColor(String value) {
        try {
            Color.parseColor(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
