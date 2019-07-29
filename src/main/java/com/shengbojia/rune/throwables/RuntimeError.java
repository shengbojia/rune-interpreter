package com.shengbojia.rune.throwables;

import com.shengbojia.rune.token.Token;

/**
 * Runtime exception that also keeps track of the token where the error occurs.
 */
public class RuntimeError extends RuntimeException {

    public final Token token;

    public RuntimeError(Token token, String msg) {
        super(msg);
        this.token = token;
    }
}
