package com.shengbojia.lox.callables;

import com.shengbojia.lox.throwables.RuntimeError;
import com.shengbojia.lox.token.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass loxClass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass loxClass) {
        this.loxClass = loxClass;
    }

    public Object getProperty(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        throw new RuntimeError(name, "No such property found: '" + name.lexeme + "' .");
    }

    @Override
    public String toString() {
        return loxClass.name + " instance";
    }
}
