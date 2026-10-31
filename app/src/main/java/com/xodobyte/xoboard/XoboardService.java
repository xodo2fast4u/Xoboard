package com.xodobyte.xoboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class XoboardService extends InputMethodService {

    private static final String[] LETTER_ROWS = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
    private static final String[] SYMBOL_ROWS = {"1234567890", "@#$_&-+()/", "*\"':;!?"};
    private static final int MODE_KEYS = 0;
    private static final int MODE_CLIP = 1;
    private static final int MODE_FONTS = 2;
    private static final int MODE_EMOJI = 3;
    private static final int LONG_PRESS_MS = 450;
    private static final int REPEAT_DELAY_MS = 400;
    private static final int REPEAT_INTERVAL_MS = 60;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextView> letterKeys = new ArrayList<>();
    private final List<LinearLayout> styleCells = new ArrayList<>();
    private final Runnable capsRunnable = this::onCapsLongPress;

    private ClipboardManager clipboard;
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;

    private Theme theme;
    private LinearLayout root;
    private FrameLayout body;
    private LinearLayout clipList;
    private ImageView shiftKey;

    private boolean shifted;
    private boolean capsLock;
    private boolean symbols;
    private boolean shiftLongPressed;
    private int mode;
    private int activeStyle;
    private int emojiCategory = 1;


    @Override
    public void onCreate() {
        super.onCreate();
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipListener = () -> {
            try {
                ClipData data = clipboard.getPrimaryClip();
                if (data != null && data.getItemCount() > 0) {
                    CharSequence text = data.getItemAt(0).coerceToText(this);
                    if (text != null) {
                        ClipStore.add(this, text.toString());
                    }
                    if (mode == MODE_CLIP) {
                        refreshClips();
                    }
                }
            } catch (SecurityException ignored) {
            }
        };
        clipboard.addPrimaryClipChangedListener(clipListener);
    }

    @Override
    public void onDestroy() {
        if (clipboard != null && clipListener != null) {
            clipboard.removePrimaryClipChangedListener(clipListener);
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return root;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        mode = MODE_KEYS;
        symbols = false;
        capsLock = false;
        activeStyle = XoPrefs.get(this).getInt(XoPrefs.KEY_ACTIVE_STYLE, 0);
        shifted = wantsCapital(info);
        buildAll();
    }

    private boolean wantsCapital(EditorInfo info) {
        if (info == null || !XoPrefs.get(this).getBoolean(XoPrefs.KEY_AUTOCAP, true)) {
            return false;
        }
        InputConnection ic = getCurrentInputConnection();
        return ic != null && ic.getCursorCapsMode(info.inputType) != 0;
    }

    private boolean wantsCapitalNow() {
        return wantsCapital(getCurrentInputEditorInfo());
    }

    private void buildAll() {
        if (root == null) {
            return;
        }
        theme = Theme.resolve(this);
        root.removeAllViews();
        root.setBackgroundColor(theme.background);
        root.setPadding(dp(4), dp(6), dp(4), dp(6));
        root.addView(buildToolbar());

        body = new FrameLayout(this);
        LinearLayout keys = buildKeys();
        ScrollView clip = buildClipPanel();
        ScrollView fonts = buildFontPanel();
        LinearLayout emoji = buildEmojiPanel();
        keys.setVisibility(mode == MODE_KEYS ? View.VISIBLE : View.GONE);
        emoji.setVisibility(mode == MODE_EMOJI ? View.VISIBLE : View.GONE);
        clip.setVisibility(mode == MODE_CLIP ? View.VISIBLE : View.GONE);
        fonts.setVisibility(mode == MODE_FONTS ? View.VISIBLE : View.GONE);
        body.addView(keys);
        body.addView(clip);
        body.addView(fonts);
        body.addView(emoji);
        root.addView(body);
    }

    private LinearLayout buildToolbar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);

        ImageView clip = iconButton(mode == MODE_CLIP ? R.drawable.ic_keyboard : R.drawable.ic_clipboard,
                mode == MODE_CLIP ? theme.accent : theme.special,
                mode == MODE_CLIP ? 0xFFFFFFFF : theme.text);
        clip.setContentDescription("Clipboard");
        clip.setOnClickListener(v -> {
            mode = mode == MODE_CLIP ? MODE_KEYS : MODE_CLIP;
            buildAll();
        });

        ImageView fonts = iconButton(R.drawable.ic_font,
                mode == MODE_FONTS ? theme.accent : theme.special,
                mode == MODE_FONTS ? 0xFFFFFFFF : theme.text);
        fonts.setContentDescription("Text styles");
        fonts.setOnClickListener(v -> {
            mode = mode == MODE_FONTS ? MODE_KEYS : MODE_FONTS;
            buildAll();
        });


        ImageView settings = iconButton(R.drawable.ic_sliders, theme.special, theme.text);
        settings.setContentDescription("Settings");
        settings.setOnClickListener(v -> openSettings());

        ImageView hide = iconButton(R.drawable.ic_hide, theme.special, theme.text);
        hide.setContentDescription("Hide keyboard");
        hide.setOnClickListener(v -> requestHideSelf(0));

        bar.addView(clip, lp(0, dp(40), 1f));
        bar.addView(fonts, lp(0, dp(40), 1f));
        bar.addView(settings, lp(0, dp(40), 1f));
        bar.addView(hide, lp(0, dp(40), 1f));
        return bar;
    }

    private LinearLayout buildKeys() {
        letterKeys.clear();
        shiftKey = null;
        LinearLayout area = new LinearLayout(this);
        area.setOrientation(LinearLayout.VERTICAL);
        area.setPadding(0, dp(4), 0, 0);

        String[] rows = symbols ? SYMBOL_ROWS : LETTER_ROWS;
        for (int r = 0; r < rows.length; r++) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            if (r == 1 && !symbols) {
                line.addView(spacer(0.5f));
            }
            if (r == 2 && !symbols) {
                line.addView(buildShift());
            }
            for (int c = 0; c < rows[r].length(); c++) {
                final String label = String.valueOf(rows[r].charAt(c));
                TextView key = keyView(label, theme.key, theme.text, 1f);
                key.setTag(label);
                if (!symbols) {
                    letterKeys.add(key);
                }
                bindKey(key, () -> onText(label), false);
                line.addView(key);
            }
            if (r == 1 && !symbols) {
                line.addView(spacer(0.5f));
            }
            if (r == 2) {
                line.addView(backspaceKey());
            }
            area.addView(line);
        }
        area.addView(bottomRow());
        updateLabels();
        return area;
    }

    private ImageView buildShift() {
        shiftKey = iconKey(R.drawable.ic_shift, theme.special, theme.text, 1.5f);
        shiftKey.setOnTouchListener((v, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    shiftLongPressed = false;
                    handler.postDelayed(capsRunnable, LONG_PRESS_MS);
                    v.setAlpha(0.6f);
                    return true;
                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(capsRunnable);
                    if (!shiftLongPressed) {
                        haptic(v);
                        toggleShift();
                    }
                    v.setAlpha(1f);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(capsRunnable);
                    v.setAlpha(1f);
                    return true;
                default:
                    return true;
            }
        });
        return shiftKey;
    }

    private View backspaceKey() {
        ImageView key = iconKey(R.drawable.ic_backspace, theme.special, theme.text, 1.5f);
        bindKey(key, this::deleteOne, true);
        return key;
    }

    private LinearLayout bottomRow() {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER);

        TextView modeKey = keyView(symbols ? "ABC" : "?123", theme.special, theme.text, 1.5f);
        modeKey.setTextSize(16f);
        bindKey(modeKey, () -> {
            symbols = !symbols;
            buildAll();
        }, false);

        TextView comma = keyView(",", theme.key, theme.text, 1f);
        bindKey(comma, () -> onText(","), false);

        TextView space = keyView("", theme.key, theme.text, 4f);
        bindKey(space, this::onSpace, false);

        TextView period = keyView(".", theme.key, theme.text, 1f);
        bindKey(period, () -> onText("."), false);

        boolean canSend = enterPerformsAction();
        ImageView enter = iconKey(canSend ? R.drawable.ic_send : R.drawable.ic_enter,
                theme.accent, 0xFFFFFFFF, 1.5f);
        enter.setContentDescription(canSend ? "Search or send" : "New line");
        bindEnter(enter);

        ImageView emojiKey = iconKey(R.drawable.ic_emoji, theme.special, theme.text, 1f);
        emojiKey.setContentDescription("Emoji");
        bindKey(emojiKey, () -> {
            mode = MODE_EMOJI;
            emojiCategory = recentEmojis().isEmpty() ? 1 : 0;
            buildAll();
        }, false);

        line.addView(modeKey);
        line.addView(emojiKey);
        line.addView(comma);
        line.addView(space);
        line.addView(period);
        line.addView(enter);
        return line;
    }

    private void bindKey(View key, Runnable action, boolean repeat) {
        final Runnable again = new Runnable() {
            @Override
            public void run() {
                haptic(key);
                action.run();
                handler.postDelayed(this, REPEAT_INTERVAL_MS);
            }
        };
        key.setOnTouchListener((v, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.6f);
                    haptic(v);
                    action.run();
                    if (repeat) {
                        handler.postDelayed(again, REPEAT_DELAY_MS);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f);
                    handler.removeCallbacks(again);
                    return true;
                default:
                    return true;
            }
        });
    }

    private void onCapsLongPress() {
        shiftLongPressed = true;
        capsLock = !capsLock;
        shifted = capsLock;
        updateLabels();
        haptic();
    }

    private void toggleShift() {
        if (capsLock) {
            capsLock = false;
            shifted = false;
        } else {
            shifted = !shifted;
        }
        updateLabels();
    }

    private void updateLabels() {
        boolean upper = shifted || capsLock;
        for (TextView key : letterKeys) {
            String base = (String) key.getTag();
            key.setText(upper ? base.toUpperCase() : base);
        }
        if (shiftKey != null) {
            shiftKey.setImageResource(capsLock ? R.drawable.ic_caps : R.drawable.ic_shift);
            shiftKey.setBackground(rounded(upper ? theme.accent : theme.special, 8));
            shiftKey.setColorFilter(upper ? 0xFFFFFFFF : theme.text, PorterDuff.Mode.SRC_IN);
        }
    }

    private void onText(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        String out = s;
        if (!symbols && (shifted || capsLock)) {
            out = out.toUpperCase();
        }
        if (activeStyle > 0) {
            out = FontStyles.apply(activeStyle, out);
        }
        ic.commitText(out, 1);
        if (shifted && !capsLock) {
            shifted = false;
            updateLabels();
        }
    }

    private void onSpace() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        boolean doubleSpace = XoPrefs.get(this).getBoolean(XoPrefs.KEY_DOUBLE_SPACE, true);
        CharSequence before = doubleSpace ? ic.getTextBeforeCursor(2, 0) : null;
        if (doubleSpace && before != null && before.length() == 2
                && before.charAt(1) == ' ' && Character.isLetterOrDigit(before.charAt(0))) {
            ic.deleteSurroundingText(1, 0);
            ic.commitText(". ", 1);
        } else {
            ic.commitText(" ", 1);
        }
        afterBoundary();
    }

    private boolean enterPerformsAction() {
        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null || (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return false;
        }
        return editorAction() != EditorInfo.IME_ACTION_NONE;
    }

    private void onEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        if (enterPerformsAction()) {
            ic.performEditorAction(editorAction());
        } else {
            ic.commitText("\n", 1);
        }
        afterBoundary();
    }

    private void newLine() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText("\n", 1);
        }
        afterBoundary();
    }

    private int editorAction() {
        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null) {
            return EditorInfo.IME_ACTION_NONE;
        }
        int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;
        if (action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            return EditorInfo.IME_ACTION_NONE;
        }
        return action;
    }

    private void bindEnter(View key) {
        final boolean[] longFired = {false};
        final Runnable newlineRunnable = () -> {
            longFired[0] = true;
            newLine();
        };
        key.setOnTouchListener((v, e) -> {
            int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                longFired[0] = false;
                v.setAlpha(0.6f);
                haptic(v);
                handler.postDelayed(newlineRunnable, LONG_PRESS_MS);
            } else if (action == MotionEvent.ACTION_UP) {
                handler.removeCallbacks(newlineRunnable);
                v.setAlpha(1f);
                if (!longFired[0]) {
                    onEnter();
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacks(newlineRunnable);
                v.setAlpha(1f);
            }
            return true;
        });
    }

    private void afterBoundary() {
        if (!capsLock) {
            shifted = wantsCapitalNow();
            updateLabels();
        }
    }

    private void deleteOne() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        CharSequence selected = ic.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            ic.commitText("", 1);
        } else {
            ic.deleteSurroundingText(clusterLengthBeforeCursor(ic), 0);
        }
    }

    private int clusterLengthBeforeCursor(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(16, 0);
        if (before == null || before.length() == 0) {
            return 1;
        }
        int i = before.length();
        while (i > 0) {
            int cp = Character.codePointBefore(before, i);
            if (Character.getType(cp) != Character.NON_SPACING_MARK) {
                break;
            }
            i -= Character.charCount(cp);
        }
        if (i > 0) {
            i -= Character.charCount(Character.codePointBefore(before, i));
        }
        return before.length() - i;
    }

    private final Map<Integer, List<String>> emojiCache = new HashMap<>();


    private LinearLayout buildEmojiPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(3), dp(3), dp(3), dp(3));

        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        for (int t = 0; t <= EmojiData.CATEGORY_DATA.length; t++) {
            final int index = t;
            String icon;
            if (t == 0) {
                icon = emojiText("1F558");
            } else {
                List<String> tabItems = emojiItems(t);
                icon = tabItems.isEmpty() ? "?" : tabItems.get(0);
            }
            TextView tab = chip(icon,
                    t == emojiCategory ? theme.accent : theme.special, theme.text);
            tab.setTextSize(18f);
            tab.setOnClickListener(v -> {
                emojiCategory = index;
                buildAll();
            });
            tabs.addView(tab, lp(dp(48), dp(36), 0));
        }
        tabScroll.addView(tabs);
        panel.addView(tabScroll);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(200)));
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        List<String> items = emojiItems(emojiCategory);
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nothing used yet");
            empty.setTextColor(theme.text);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            grid.addView(empty, lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        }
        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % 8 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row, lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            }
            final String emoji = items.get(i);
            TextView cell = new TextView(this);
            cell.setText(emoji);
            cell.setTextSize(24f);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, dp(4), 0, dp(4));
            cell.setOnClickListener(v -> commitEmoji(emoji));
            row.addView(cell, lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        if (row != null) {
            while (row.getChildCount() < 8) {
                View filler = new View(this);
                row.addView(filler, new LinearLayout.LayoutParams(0, 0, 1f));
            }
        }
        scroll.addView(grid);
        panel.addView(scroll);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        TextView letters = chip("ABC", theme.special, theme.text);
        letters.setOnClickListener(v -> {
            mode = MODE_KEYS;
            buildAll();
        });
        ImageView back = iconButton(R.drawable.ic_backspace, theme.special, theme.text);
        back.setContentDescription("Delete");
        bindKey(back, this::deleteOne, true);
        bar.addView(letters, lp(0, dp(40), 1f));
        bar.addView(back, lp(0, dp(40), 1f));
        panel.addView(bar);
        return panel;
    }

    private List<String> emojiItems(int tab) {
        if (tab == 0) {
            return recentEmojis();
        }
        List<String> cached = emojiCache.get(tab);
        if (cached != null) {
            return cached;
        }
        Paint paint = new Paint();
        List<String> out = new ArrayList<>();
        for (String token : EmojiData.CATEGORY_DATA[tab - 1].split(" ")) {
            if (token.isEmpty()) {
                continue;
            }
            String text = emojiText(token);
            if (paint.hasGlyph(text)) {
                out.add(text);
            }
        }
        emojiCache.put(tab, out);
        return out;
    }

    private List<String> recentEmojis() {
        List<String> out = new ArrayList<>();
        String raw = XoPrefs.get(this).getString(XoPrefs.KEY_RECENT_EMOJI, "");
        if (!raw.isEmpty()) {
            for (String e : raw.split(",")) {
                if (!e.isEmpty()) {
                    out.add(e);
                }
            }
        }
        return out;
    }

    private void rememberEmoji(String emoji) {
        List<String> list = recentEmojis();
        list.remove(emoji);
        list.add(0, emoji);
        while (list.size() > 32) {
            list.remove(list.size() - 1);
        }
        XoPrefs.get(this).edit()
                .putString(XoPrefs.KEY_RECENT_EMOJI, TextUtils.join(",", list))
                .commit();
    }

    private String emojiText(String hex) {
        StringBuilder sb = new StringBuilder();
        for (String part : hex.split("_")) {
            sb.appendCodePoint(Integer.parseInt(part, 16));
        }
        return sb.toString();
    }

    private void commitEmoji(String emoji) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(emoji, 1);
        }
        rememberEmoji(emoji);
        haptic();
    }

    private ScrollView buildFontPanel() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(260)));
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(3), dp(3), dp(3), dp(3));

        styleCells.clear();
        LinearLayout row = null;
        for (int i = 0; i < FontStyles.count(); i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                column.addView(row, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            }
            final int index = i;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(4), dp(10), dp(4), dp(8));

            TextView preview = new TextView(this);
            preview.setText(FontStyles.apply(i, "Aa1"));
            preview.setTextSize(19f);
            preview.setGravity(Gravity.CENTER);

            TextView name = new TextView(this);
            name.setText(FontStyles.NAMES[i]);
            name.setTextSize(11f);
            name.setGravity(Gravity.CENTER);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);

            cell.addView(preview);
            cell.addView(name);
            cell.setOnClickListener(v -> selectStyle(index));
            styleCells.add(cell);
            row.addView(cell, lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        scroll.addView(column);
        updateStyleChips();
        return scroll;
    }

    private void updateStyleChips() {
        for (int i = 0; i < styleCells.size(); i++) {
            boolean on = i == activeStyle;
            LinearLayout cell = styleCells.get(i);
            cell.setBackground(rounded(on ? theme.accent : theme.key, 10));
            int fg = on ? 0xFFFFFFFF : theme.text;
            ((TextView) cell.getChildAt(0)).setTextColor(fg);
            ((TextView) cell.getChildAt(1)).setTextColor(fg);
        }
    }

    private void selectStyle(int index) {
        activeStyle = index;
        XoPrefs.get(this).edit().putInt(XoPrefs.KEY_ACTIVE_STYLE, index).apply();
        updateStyleChips();
        if (index > 0) {
            convertLastWord(index);
        }
        haptic();
    }

    private void convertLastWord(int index) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        CharSequence selected = ic.getSelectedText(0);
        if (selected != null && selected.length() > 0) {
            ic.commitText(FontStyles.apply(index, selected.toString()), 1);
            return;
        }
        CharSequence before = ic.getTextBeforeCursor(64, 0);
        if (before == null) {
            return;
        }
        int end = before.length();
        int start = end;
        while (start > 0 && !Character.isWhitespace(before.charAt(start - 1))) {
            start--;
        }
        if (start == end) {
            return;
        }
        String word = before.subSequence(start, end).toString();
        ic.deleteSurroundingText(word.length(), 0);
        ic.commitText(FontStyles.apply(index, word), 1);
    }

    private ScrollView buildClipPanel() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(260)));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(3), dp(3), dp(3), dp(3));

        int limit = ClipStore.limit(this);
        TextView header = chip("Clear all   limit " + (limit == 0 ? "unlimited" : String.valueOf(limit)),
                theme.special, theme.text);
        header.setOnClickListener(v -> {
            ClipStore.clear(this);
            refreshClips();
        });
        column.addView(header, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(36), 0));

        clipList = new LinearLayout(this);
        clipList.setOrientation(LinearLayout.VERTICAL);
        column.addView(clipList);

        scroll.addView(column);
        refreshClips();
        return scroll;
    }

    private void refreshClips() {
        if (clipList == null || theme == null) {
            return;
        }
        clipList.removeAllViews();
        List<String> items = ClipStore.load(this);
        if (items.isEmpty()) {
            TextView empty = chip("Nothing copied yet", theme.key, theme.text);
            clipList.addView(empty, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(44), 0));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            final String item = items.get(i);
            final int index = i;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView text = chip(item, theme.key, theme.text);
            text.setMaxLines(2);
            text.setEllipsize(TextUtils.TruncateAt.END);
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            text.setOnClickListener(v -> commitClip(item));
            row.addView(text, lp(0, dp(44), 1f));

            ImageView delete = iconButton(R.drawable.ic_close, theme.special, theme.text);
            delete.setContentDescription("Delete");
            delete.setOnClickListener(v -> {
                ClipStore.remove(this, index);
                refreshClips();
            });
            row.addView(delete, lp(dp(44), dp(44), 0));

            clipList.addView(row, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        }
    }

    private void commitClip(String item) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(item, 1);
            haptic();
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requestHideSelf(0);
    }

    private TextView keyView(String label, int background, int foreground, float weight) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(foreground);
        t.setTextSize(20f);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(background, 8));
        t.setLayoutParams(lp(0, dp(50), weight));
        return t;
    }

    private ImageView iconKey(int resId, int background, int foreground, float weight) {
        ImageView v = iconButton(resId, background, foreground);
        v.setLayoutParams(lp(0, dp(50), weight));
        return v;
    }

    private ImageView iconButton(int resId, int background, int foreground) {
        ImageView v = new ImageView(this);
        Drawable drawable = getDrawable(resId);
        if (drawable != null) {
            v.setImageDrawable(drawable.mutate());
        }
        v.setColorFilter(foreground, PorterDuff.Mode.SRC_IN);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setPadding(dp(10), dp(10), dp(10), dp(10));
        v.setBackground(rounded(background, 8));
        v.setLayoutParams(lp(0, dp(40), 1f));
        return v;
    }

    private TextView chip(String label, int background, int foreground) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(foreground);
        t.setTextSize(16f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), 0, dp(10), 0);
        t.setBackground(rounded(background, 10));
        return t;
    }

    private View spacer(float weight) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, dp(50), weight));
        return v;
    }

    private LinearLayout.LayoutParams lp(int width, int height, float weight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        return params;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private void haptic() {
        haptic(root);
    }

    private void haptic(View view) {
        if (view != null && XoPrefs.get(this).getBoolean(XoPrefs.KEY_HAPTIC, true)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
