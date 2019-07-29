package com.shengbojia.rune.callables;

import com.shengbojia.rune.throwables.RuntimeError;
import com.shengbojia.rune.token.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Similar to Python, I will be allowing for fields to be added dynamically to object instances.
 */
public class RuneInstance {
    private RuneClass runeClass;
    private final Map<String, Object> fields = new HashMap<>();

    RuneInstance(RuneClass runeClass) {
        this.runeClass = runeClass;
    }

    public Object getProperty(Token name) {

        // Look for fields first, want fields to shadow methods
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // If not matching field is found, try looking for a method
        RuneFunction method = runeClass.findMethod(name.lexeme);
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
        return runeClass.name + " instance";
    }
}
