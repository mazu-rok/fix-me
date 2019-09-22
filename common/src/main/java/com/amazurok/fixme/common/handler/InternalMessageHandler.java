package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;

import java.nio.channels.AsynchronousSocketChannel;

public class InternalMessageHandler extends MessageHandler {
    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        if (!message.startsWith(Common.INTERNAL_MESSAGE)) {
            super.handle(clientChannel, message);
        }
    }
}
