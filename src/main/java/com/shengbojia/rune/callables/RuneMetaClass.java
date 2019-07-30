package com.shengbojia.rune.callables;

import java.util.Map;

public class RuneMetaClass implements RuneClassDesc {
    public final String name;
    private final RuneClassDesc superClass;
    private final Map<String, RuneFunction> classMethods;

    public RuneMetaClass(String name, RuneClassDesc superClass, Map<String, RuneFunction> classMethods) {
        this.name = name;
        this.superClass = superClass;
        this.classMethods = classMethods;
    }

    public RuneMetaClass(RuneClass runeClass, Map<String, RuneFunction> classMethods) {
        this.name = runeClass.name;
        this.superClass = (RuneMetaClass) (runeClass.runeClassDesc);
        this.classMethods = classMethods;
    }

    @Override
    public RuneFunction findMethod(String name) {
        if (classMethods.containsKey(name)) {
            return classMethods.get(name);
        }

        // Class methods are inherited as well
        if (superClass != null) {
            return superClass.findMethod(name);
        }

        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
