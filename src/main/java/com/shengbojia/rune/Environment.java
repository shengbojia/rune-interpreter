package com.shengbojia.rune;

import com.shengbojia.rune.throwables.RuntimeError;
import com.shengbojia.rune.token.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper of a hash map which stores variables as identifier-value pairs.
 */
public class Environment {

    private Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Adds a identifier-value pair to the environment. Allows for redefining of a global variable.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     */
    public void define(String name, Object value) {
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

        // Check if variable is defined in the enclosing scope (if there is one)
        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Looks up a variable at the specified environment up the chain.
     *
     * @param distance the location of the environment up the chain
     * @param name     the name of the variable to look for
     * @return the variable
     */
    public Object getAt(int distance, String name) {
        return moveUp(distance).values.get(name);
    }

    /**
     * Walks up the specified numbers of steps up the chain and returns the environment there.
     *
     * @param distance number of steps to move up
     * @return the environment found
     */
    Environment moveUp(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // Check if variable is defined in the enclosing scope (if there is one)
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int distance, Token name, Object value) {
        moveUp(distance).values.put(name.lexeme, value);
    }
}
