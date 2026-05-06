package com.elfmcys.yesstevemodel.molang.lexer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Class representing a Molang token. Each token has some
 * information set by the lexer (i.e. start/end position,
 * token kind and optional value)
 *
 * @since 3.0.0
 */
public final class Token {
    private final TokenKind kind;
    private final @Nullable String value;
    private final int start;
    private final int end;

    public Token(
            final @NotNull TokenKind kind,
            final @Nullable String value,
            final int start,
            final int end
    ) {
        this.kind = requireNonNull(kind, "kind");
        this.value = value;
        this.start = start;
        this.end = end;

        // verify state, token kinds that have HAS_VALUE tag, must have a non-null value
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE) && value == null) {
            throw new IllegalArgumentException("A token with kind "
                    + kind + " must have a non-null value");
        }
    }

    /**
     * Gets the token kind.
     *
     * @return The token kind
     * @since 3.0.0
     */
    public @NotNull TokenKind kind() {
        return kind;
    }

    /**
     * Gets the token value. Null if this kind
     * of tokens doesn't allow values.
     *
     * @return The token value
     * @since 3.0.0
     */
    public String value() {
        return value;
    }

    /**
     * Gets the start index of this token.
     *
     * @return The token start
     * @since 3.0.0
     */
    public int start() {
        return start;
    }

    /**
     * Gets the end index of this token.
     *
     * @return The token end
     * @since 3.0.0
     */
    public int end() {
        return end;
    }

    @Override
    public String toString() {
        if (kind.hasTag(TokenKind.Tag.HAS_VALUE)) {
            return kind + "(" + value + ")";
        } else {
            return kind.toString();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        if (start != token.start) return false;
        if (end != token.end) return false;
        if (kind != token.kind) return false;
        return Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}