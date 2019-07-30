package com.shengbojia.rune.callables;

public interface RuneClassDesc {
    RuneFunction findMethod(String name);

    String getName();
}
