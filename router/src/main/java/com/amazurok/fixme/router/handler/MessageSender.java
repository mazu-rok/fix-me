package com.amazurok.fixme.router.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.NotFoundException;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageSender extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final Map<String, String> failedMessages;

    public MessageSender(Map<String, AsynchronousSocketChannel> routingTable,
                         Map<String, String> failedMessages) {
        this.routingTable = routingTable;
        this.failedMessages = failedMessages;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        log.info("Sending message: " + message);
        try {
            final String dstName = Common.getValueFromFIXMesage(message, FIXMessage.DST);
            final AsynchronousSocketChannel channel = routingTable.get(dstName);
            if (channel != null) {
                Common.sendMessage(channel, message);
                super.handle(clientChannel, message);
            } else {
                Common.sendErrorMessage(clientChannel,
                        String.format("Client with name '%s' not found", dstName));
                failedMessages.put(dstName, message);
            }
        } catch (NotFoundException e) {
            log.error(e.getMessage());
        }
    }
}

