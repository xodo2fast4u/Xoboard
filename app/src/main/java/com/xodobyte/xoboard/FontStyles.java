package com.xodobyte.xoboard;

import java.util.function.IntFunction;

final class FontStyles {

    static final String[] NAMES = {
            "Normal", "Bold", "Italic", "Bold Italic", "Sans", "Sans Bold", "Sans Italic",
            "Sans Bold Italic", "Script Bold", "Fraktur Bold", "Double Struck", "Monospace",
            "Small Caps", "Superscript", "Subscript", "Circled", "Fullwidth", "Strikethrough",
            "Underline", "Slash", "Overline"
    };

    private static final String[] SMALL = {
            "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ",
            "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"
    };

    private static final String[] SUPER = {
            "ᵃ", "ᵇ", "ᶜ", "ᵈ", "ᵉ", "ᶠ", "ᵍ", "ʰ", "ⁱ", "ʲ", "ᵏ", "ˡ", "ᵐ",
            "ⁿ", "ᵒ", "ᵖ", "q", "ʳ", "ˢ", "ᵗ", "ᵘ", "ᵛ", "ʷ", "ˣ", "ʸ", "ᶻ"
    };

    private static final String[] SUPER_DIGITS = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};

    private static final String[] SUB = {
            "ₐ", "b", "c", "d", "ₑ", "f", "g", "ₕ", "ᵢ", "ⱼ", "ₖ", "ₗ", "ₘ",
            "ₙ", "ₒ", "ₚ", "q", "ᵣ", "ₛ", "ₜ", "ᵤ", "ᵥ", "w", "ₓ", "y", "z"
    };

    private static final String[] SUB_DIGITS = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};

    private FontStyles() {
    }

    static int count() {
        return NAMES.length;
    }

    static String apply(int index, String text) {
        switch (index) {
            case 1:
                return each(text, cp -> offset(cp, 0x1D400, 0x1D41A, 0x1D7CE));
            case 2:
                return each(text, cp -> cp == 'h' ? "\u210E" : offset(cp, 0x1D434, 0x1D44E, 0));
            case 3:
                return each(text, cp -> offset(cp, 0x1D468, 0x1D482, 0));
            case 4:
                return each(text, cp -> offset(cp, 0x1D5A0, 0x1D5BA, 0x1D7E2));
            case 5:
                return each(text, cp -> offset(cp, 0x1D5D4, 0x1D5EE, 0x1D7EC));
            case 6:
                return each(text, cp -> offset(cp, 0x1D608, 0x1D622, 0));
            case 7:
                return each(text, cp -> offset(cp, 0x1D63C, 0x1D656, 0));
            case 8:
                return each(text, cp -> offset(cp, 0x1D4D0, 0x1D4EA, 0));
            case 9:
                return each(text, cp -> offset(cp, 0x1D56C, 0x1D586, 0));
            case 10:
                return each(text, FontStyles::doubleStruck);
            case 11:
                return each(text, cp -> offset(cp, 0x1D670, 0x1D68A, 0x1D7F6));
            case 12:
                return each(text, FontStyles::smallCaps);
            case 13:
                return each(text, FontStyles::superscript);
            case 14:
                return each(text, FontStyles::subscript);
            case 15:
                return each(text, FontStyles::circled);
            case 16:
                return each(text, cp -> offset(cp, 0xFF21, 0xFF41, 0xFF10));
            case 17:
                return combine(text, "\u0336");
            case 18:
                return combine(text, "\u0332");
            case 19:
                return combine(text, "\u0338");
            case 20:
                return combine(text, "\u0305");
            default:
                return text;
        }
    }

    private static String each(String text, IntFunction<String> map) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            sb.append(map.apply(cp));
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    private static String combine(String text, String mark) {
        return each(text, cp -> Character.isWhitespace(cp) ? cps(cp) : cps(cp) + mark);
    }

    private static String offset(int cp, int upper, int lower, int digit) {
        if (upper != 0 && cp >= 'A' && cp <= 'Z') {
            return cps(upper + cp - 'A');
        }
        if (lower != 0 && cp >= 'a' && cp <= 'z') {
            return cps(lower + cp - 'a');
        }
        if (digit != 0 && cp >= '0' && cp <= '9') {
            return cps(digit + cp - '0');
        }
        return cps(cp);
    }

    private static String doubleStruck(int cp) {
        switch (cp) {
            case 'C': return "\u2102";
            case 'H': return "\u210D";
            case 'N': return "\u2115";
            case 'P': return "\u2119";
            case 'Q': return "\u211A";
            case 'R': return "\u211D";
            case 'Z': return "\u2124";
            default: return offset(cp, 0x1D538, 0x1D552, 0x1D7D8);
        }
    }

    private static String smallCaps(int cp) {
        if (cp >= 'a' && cp <= 'z') {
            return SMALL[cp - 'a'];
        }
        if (cp >= 'A' && cp <= 'Z') {
            return SMALL[cp - 'A'];
        }
        return cps(cp);
    }

    private static String superscript(int cp) {
        if (cp >= 'a' && cp <= 'z') {
            return SUPER[cp - 'a'];
        }
        if (cp >= '0' && cp <= '9') {
            return SUPER_DIGITS[cp - '0'];
        }
        return cps(cp);
    }

    private static String subscript(int cp) {
        if (cp >= 'a' && cp <= 'z') {
            return SUB[cp - 'a'];
        }
        if (cp >= '0' && cp <= '9') {
            return SUB_DIGITS[cp - '0'];
        }
        return cps(cp);
    }

    private static String circled(int cp) {
        if (cp == '0') {
            return "\u24EA";
        }
        if (cp >= '1' && cp <= '9') {
            return cps(0x2460 + cp - '1');
        }
        return offset(cp, 0x24B6, 0x24D0, 0);
    }

    private static String cps(int cp) {
        return new String(Character.toChars(cp));
    }
}
