package com.shengbojia.rune.callables;

import com.shengbojia.rune.Interpreter;

import java.util.List;

public interface RuneCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
