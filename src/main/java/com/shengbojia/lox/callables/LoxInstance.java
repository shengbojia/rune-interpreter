package com.shengbojia.lox.callables;

public class LoxInstance {
    private LoxClass loxClass;

    LoxInstance(LoxClass loxClass) {
        this.loxClass = loxClass;
    }

    @Override
    public String toString() {
        return loxClass.name + " instance";
    }
}
