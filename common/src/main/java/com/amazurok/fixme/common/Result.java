package com.amazurok.fixme.common;

public enum Result {
    Executed,
    Rejected;

    public static boolean is(String result) {
        return result.equals(Executed.toString()) || result.equals(Rejected.toString());
    }
}
