package com.elfmcys.yesstevemodel.molang.parser;

import com.elfmcys.yesstevemodel.molang.lexer.Cursor;

import java.io.IOException;

/**
 * Exception that can be thrown during the
 * parsing phase
 *
 * @since 3.0.0
 */
public class ParseException extends IOException {

    private final Cursor cursor;

    public ParseException(Cursor cursor) {
        this.cursor = cursor;
    }

    public ParseException(String str, Cursor cursor) {
        super(appendCursor(str, cursor));
        this.cursor = cursor;
    }

    public ParseException(Throwable th, Cursor cursor) {
        super(th);
        this.cursor = cursor;
    }

    public ParseException(String str, Throwable th, Cursor cursor) {
        super(appendCursor(str, cursor), th);
        this.cursor = cursor;
    }

    public Cursor cursor() {
        return this.cursor;
    }

    private static String appendCursor(String message, Cursor cursor) {
        if (cursor == null) return message; // todo
        // default format for exception messages, i.e.
        // "unexpected token: '%'"
        // "    at line 2, column 6"
        return message + "\n  at " + cursor;
    }
}