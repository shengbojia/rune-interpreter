package com.shengbojia.rune.throwables;

public class Return extends RuntimeException {
    public final Object value;

    public Return(Object value) {
        // Since this isn't actually an error, just a return value, don't need some of the machinery
        super(null, null, false, false);
        this.value = value;
    }
}
