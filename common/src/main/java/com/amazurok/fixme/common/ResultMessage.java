package com.amazurok.fixme.common;

public enum ResultMessage {
    EXECUTED,
    REJECTED;

    public static boolean is(String result) {
        return result.toUpperCase().equals(EXECUTED.toString()) || result.toUpperCase().equals(REJECTED.toString());
    }
}
