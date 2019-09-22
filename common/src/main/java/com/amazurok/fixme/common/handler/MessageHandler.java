package com.amazurok.fixme.common.handler;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class MessageHandler {
    private MessageHandler next;

    public final void setNext(MessageHandler next) {
        this.next = next;
    }

    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        if (next != null) {
            next.handle(clientChannel, message);
        }
    }
}
