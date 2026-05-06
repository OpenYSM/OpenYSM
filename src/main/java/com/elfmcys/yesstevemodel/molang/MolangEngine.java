package com.elfmcys.yesstevemodel.molang;

import com.elfmcys.yesstevemodel.molang.lexer.Cursor;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * The engine's entry class. Provides methods to evaluate
 * and parse Molang code from strings and readers.
 *
 * @since 3.0.0
 */
public interface MolangEngine {
    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @throws ParseException If read failed or there
     *                        are syntax errors in the script
     */
    List<Expression> parse(Reader reader) throws IOException;

    /**
     * Parses the given {@code string} to a list of
     * {@link Expression}
     *
     * @param string The MoLang string
     * @return The list of parsed expressions
     * @throws ParseException If parsing fails
     */
    default List<Expression> parse(String str) throws ParseException {
        try {
            StringReader stringReader = new StringReader(str);
            List<Expression> listMo872xaffeef43 = parse(stringReader);
            stringReader.close();
            return listMo872xaffeef43;
        } catch (ParseException e) {
            throw e;
        } catch (IOException e2) {
            throw new ParseException("Failed to close string reader", e2, new Cursor(0, 0));
        }
    }

    static MolangEngine fromCustomBinding(ObjectBinding objectBinding) {
        return new MolangEngineImpl(objectBinding);
    }

    static MolangEngine createEmpty() {
        return new MolangEngineImpl(ObjectBinding.EMPTY);
    }
}