package com.shengbojia.lox.callables;

import com.shengbojia.lox.throwables.RuntimeError;
import com.shengbojia.lox.token.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Similar to Python, I will be allowing for fields to be added dynamically to object instances.
 */
public class LoxInstance {
    private LoxClass loxClass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass loxClass) {
        this.loxClass = loxClass;
    }

    public Object getProperty(Token name) {

        // Look for fields first, want fields to shadow methods
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // If not matching field is found, try looking for a method
        LoxFunction method = loxClass.findMethod(name.lexeme);
        if (method != null) {
            // a new method ref with instance ref "this" in its closure
            return method.bind(this);
        }

        throw new RuntimeError(name, "No such property found: '" + name.lexeme + "' .");
    }

    public void setField(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return loxClass.name + " instance";
    }
}
