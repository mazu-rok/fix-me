package com.amazurok.fixme.common;

public enum Result {
    EXECUTED,
    REJECTED;

    public static boolean is(String result) {
        return result.toUpperCase().equals(EXECUTED.toString()) || result.toUpperCase().equals(REJECTED.toString());
    }
}
