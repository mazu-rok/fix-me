package com.amazurok.fixme.router.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;
import com.amazurok.fixme.common.handler.MessageHandler;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageProcessor extends MessageHandler {

    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final Map<String, String> failedMessages;

    public MessageProcessor(Map<String, AsynchronousSocketChannel> routingTable,
                            Map<String, String> failedMessages) {
        this.routingTable = routingTable;
        this.failedMessages = failedMessages;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        System.out.println("Processing message: " + message);
        try {
            final String targetName = Common.getFixValueByTag(message, FixTag.TARGET_NAME);
            final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
            if (targetChannel != null) {
                Common.sendMessage(targetChannel, message);
                super.handle(clientChannel, message);
            } else {
                Common.sendInternalMessage(clientChannel,
                        "No connected client with such name: " + targetName + ", will try later");
                failedMessages.put(targetName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

