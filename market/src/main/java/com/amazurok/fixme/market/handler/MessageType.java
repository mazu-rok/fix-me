package com.amazurok.fixme.market.handler;

public enum MessageType {
    BUY,
    SELL;

    public static boolean is(String type) {
        return type.toUpperCase().equals(BUY.toString()) || type.equals(SELL.toString());
    }
}
    