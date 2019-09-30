package com.amazurok.fixme.router.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageProcessor extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final Map<String, String> failedMessages;

    public MessageProcessor(Map<String, AsynchronousSocketChannel> routingTable,
                            Map<String, String> failedMessages) {
        this.routingTable = routingTable;
        this.failedMessages = failedMessages;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        log.info("Processing message: " + message);
        try {
            final String targetName = Common.getValueFromFIXMesage(message, FIXMessage.DST);
            final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
            if (targetChannel != null) {
                Common.sendMessage(targetChannel, message);
                super.handle(clientChannel, message);
            } else {
                Common.sendErrorMessage(clientChannel,
                        "No connected client with such name: " + targetName + ", will try later");
                failedMessages.put(targetName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

