package com.elfmcys.yesstevemodel.molang.parser;

import com.elfmcys.yesstevemodel.molang.lexer.Cursor;
import com.elfmcys.yesstevemodel.molang.lexer.MolangLexer;
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the Molang language.
 *
 * <p>The parser converts token streams to expression
 * streams</p>
 *
 * <p>Note that this is a stream-based parser, this means
 * that it will not consume the entire lexer if it doesn't
 * continue having next() calls</p>
 *
 * @since 3.0.0
 */
public /* sealed */ interface MolangParser /* permits MolangParserImpl */ extends Closeable {
    /**
     * Returns the internal lexer being used.
     *
     * @return The lexer for this parser.
     * @since 3.0.0
     */
    @NotNull MolangLexer lexer();

    /**
     * Returns the cursor for this parser, the cursor maintains
     * track of the current line and column, it is used for
     * error reporting.
     *
     * @return The cursor.
     * @since 3.0.0
     */
    @NotNull default Cursor cursor() {
        return lexer().cursor();
    }

    /**
     * Returns the last emitted expression (the last expression value
     * returned when calling {@link MolangParser#next()})
     *
     * <p>Requires the user to call {@link MolangParser#next()}
     * at least once first.</p>
     *
     * @return The last emitted expression
     * @throws IllegalStateException If there is no current expression
     * @since 3.0.0
     */
    @Nullable Expression current();

    /**
     * Parses the next expression.
     *
     * <p>This method returns {@code null} if it reaches
     * the end of file and throws a {@link ParseException}
     * if there is an error.</p>
     *
     * @return The parsed expression
     * @throws IOException If reading or parsing fails
     * @since 3.0.0
     */
    @Nullable Expression next() throws IOException;

    /**
     * Parses all the tokens until it finds a {@link TokenKind#EOF}.
     *
     * <p>After this method is called, the parser should be
     * done and all next expressions will be null</p>
     *
     * @return All the read expressions
     * @throws IOException If reading or parsing fails
     * @since 3.0.0
     */
    default @NotNull List<Expression> parseAll() throws IOException {
        List<Expression> tokens = new ArrayList<>();
        Expression expr;
        while ((expr = next()) != null) {
            tokens.add(expr);
        }
        return tokens;
    }

    /**
     * Closes this parser and the internal {@link MolangLexer}.
     *
     * @throws IOException If closing fails
     * @since 3.0.0
     */
    @Override
    void close() throws IOException;

    static @NotNull MolangParser parser(@NotNull MolangLexer molangLexer, @NotNull ObjectBinding objectBinding) throws IOException {
        return new MolangParserImpl(molangLexer, objectBinding);
    }

    static @NotNull MolangParser parser(@NotNull Reader reader, @NotNull ObjectBinding objectBinding) throws IOException {
        return parser(MolangLexer.lexer(reader), objectBinding);
    }

    static @NotNull MolangParser parser(@NotNull String str, @NotNull ObjectBinding objectBinding) throws IOException {
        return parser(MolangLexer.lexer(str), objectBinding);
    }

    static @NotNull List<Expression> parseExpressions(@NotNull Reader reader, @NotNull ObjectBinding objectBinding) throws IOException {
        MolangParser molangParser = parser(reader, objectBinding);
        try {
            List<Expression> expressionList = molangParser.parseAll();
            if (molangParser != null) {
                molangParser.close();
            }
            return expressionList;
        } catch (Throwable th) {
            if (molangParser != null) {
                try {
                    molangParser.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    static @NotNull List<Expression> parseExpressions(@NotNull String str, @NotNull ObjectBinding objectBinding) throws IOException {
        MolangParser molangParser = parser(str, objectBinding);
        try {
            List<Expression> expressionList = molangParser.parseAll();
            if (molangParser != null) {
                molangParser.close();
            }
            return expressionList;
        } catch (Throwable th) {
            if (molangParser != null) {
                try {
                    molangParser.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }
}