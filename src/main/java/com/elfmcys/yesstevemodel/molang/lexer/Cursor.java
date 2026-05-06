package com.elfmcys.yesstevemodel.molang.lexer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Mutable class that tracks the position of characters
 * when performing lexical analysis
 *
 * <p>Can be used to show the position of lexical errors
 * in a human-readable way</p>
 *
 * @since 3.0.0
 */
public final class Cursor implements Cloneable {

    private int index = 0;

    private int line = 0;

    private int column = 0;

    public Cursor(final int line, final int column) {
        this.line = line;
        this.column = column;
    }

    public Cursor() {
    }

    public int index() {
        return this.index;
    }

    public int line() {
        return this.line;
    }

    public int column() {
        return this.column;
    }

    public void push(final int character) {
        index++;
        if (character == '\n') {
            // if it's a line break,
            // reset the column
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    @Override
    public @NotNull Cursor clone() {
        return new Cursor(line, column);
    }

    @Override
    public String toString() {
        return "line " + line + ", column " + column;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cursor that = (Cursor) o;
        return line == that.line && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}