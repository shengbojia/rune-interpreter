package com.shengbojia.lox;

import com.shengbojia.lox.errors.RuntimeError;
import com.shengbojia.lox.token.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper of a hash map which stores variables as identifier-value pairs.
 */
public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Adds a identifier-value pair to the environment. Allows for redefining of a global variable.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Looks up a variable from the environment based on the inputted token. Throws a runtime error if the variable
     * if accessed before being defined.
     *
     * @param name the identifier token of the desired variable
     * @return the value of the referenced variable
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
