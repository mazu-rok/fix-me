package com.amazurok.fixme.market.handler;

public enum Result {
    Executed,
    Rejected;

    public static boolean is(String result) {
        return result.equals(Executed.toString()) || result.equals(Rejected.toString());
    }
}
