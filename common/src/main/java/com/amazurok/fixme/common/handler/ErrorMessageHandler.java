package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;

import java.nio.channels.AsynchronousSocketChannel;

public class ErrorMessageHandler extends MessageHandler {
    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        if (!message.startsWith(Common.ERROR_MESSAGE)) {
            super.handle(clientChannel, message);
        }
    }
}
