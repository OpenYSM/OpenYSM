package com.elfmcys.yesstevemodel.molang.lexer;

/**
 * Utility class holding utility static
 * methods for working with character
 * tokens
 */
public final class Characters {
    private Characters() {
    }

    public static boolean isDigit(final int c) {
        return Character.isDigit(c);
    }

    public static boolean isValidForWordStart(final int c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_';
    }

    public static boolean isValidForWordContinuation(final int c) {
        return isValidForWordStart(c) || isDigit(c);
    }
}