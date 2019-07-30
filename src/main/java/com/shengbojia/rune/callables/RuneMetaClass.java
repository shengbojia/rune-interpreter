package com.shengbojia.rune.callables;

import java.util.Map;

public class RuneMetaClass implements RuneClassDesc {
    public final String name;
    private final Map<String, RuneFunction> classMethods;

    public RuneMetaClass(String name, Map<String, RuneFunction> classMethods) {
        this.name = name;
        this.classMethods = classMethods;
    }

    @Override
    public RuneFunction findMethod(String name) {
        if (classMethods.containsKey(name)) {
            return classMethods.get(name);
        }

        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
