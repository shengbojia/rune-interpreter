package com.shengbojia.lox.throwables;

public class Break extends RuntimeException {

    public Break() {
        super(null, null, false, false);
    }
}
