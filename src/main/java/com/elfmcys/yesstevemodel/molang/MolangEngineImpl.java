package com.elfmcys.yesstevemodel.molang;

import com.elfmcys.yesstevemodel.molang.parser.MolangParser;
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public final class MolangEngineImpl implements MolangEngine {

    private final ObjectBinding bindings;

    MolangEngineImpl(ObjectBinding bindings) {
        this.bindings = bindings;
    }

    @Override
    public List<Expression> parse(Reader reader) throws IOException {
        return MolangParser.parser(reader, this.bindings).parseAll();
    }
}