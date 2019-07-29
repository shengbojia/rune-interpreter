package com.shengbojia.lox.callables;

import com.shengbojia.lox.Interpreter;
import com.shengbojia.lox.Lox;

import java.util.List;

public class LoxClass implements LoxCallable {
    public final String name;

    public LoxClass(String name) {
        this.name = name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        return instance;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public String toString() {
        return name + "::class";
    }
}
